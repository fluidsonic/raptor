package io.fluidsonic.raptor.service2

import java.util.concurrent.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*


/**
 * A [CoroutineDispatcher] that runs at most one coroutine at a time, with priority-gated
 * scheduling between primary and background tasks.
 *
 * **Scheduling**
 *
 * - Primary tasks ([launchPrimaryTask]) run to completion before any other primary task or
 *   background task may start. Multiple primary tasks are serialized in arrival order.
 * - Background tasks ([launchBackgroundTask]) run only when no primary task is active and yield
 *   as soon as a new primary task becomes ready.
 *
 * All coroutines must be launched via [launchPrimaryTask] or [launchBackgroundTask].
 *
 * @param forwardDispatcher Dispatcher used to actually run coroutine blocks.
 * @param monitor Optional monitor for tracking task durations, queue depths, and dispatch times.
 * @param serviceName Human-readable name included in coroutine names and [toString].
 * @param timeSource Time source used for duration measurements.
 */
internal class ServiceDispatcher(
	private val forwardDispatcher: CoroutineDispatcher = Dispatchers.Default,
	private val monitor: ServiceDispatcherMonitor? = null,
	private val serviceName: String,
	private val timeSource: TimeSource = TimeSource.Monotonic,
) : CoroutineDispatcher() {

	private val backgroundQueue = ConcurrentLinkedQueue<Dispatch>()
	private val continuationQueue = ConcurrentLinkedQueue<Dispatch>()
	private val isForwarding = atomic(false)
	private val nextTaskId = atomic(1L)
	private val primaryQueue = ConcurrentLinkedQueue<Dispatch>()
	private var serviceScope: CoroutineScope? = null
	private var taskScope: CoroutineScope? = null

	@Volatile
	private var currentPrimaryTaskId: Long = 0L


	override fun dispatch(context: CoroutineContext, block: Runnable) {
		val task = context[Task]
			?: error("Coroutines must be launched via launchPrimaryTask() or launchBackgroundTask().")

		if (!tryDispatchFast(task, context, block)) {
			queueForTask(task).add(Dispatch(block = block, coroutineContext = context, task = task))
			forwardNext()
		}
	}


	private fun forwardNext() {
		if (isForwarding.compareAndSet(expect = false, update = true))
			forwardNextAcquired()
	}


	private fun forwardNextAcquired() {
		val next = pickNext()
		if (next != null) {
			runOnForwardDispatcher(task = next.task, coroutineContext = next.coroutineContext, block = next.block)
			return
		}

		isForwarding.value = false

		// Re-check for race: a new item may have been enqueued between pickNext() and isForwarding=false.
		// Only re-acquire if an eligible item could exist (i.e., continuationQueue or primaryQueue has items,
		// or backgroundQueue has items and no primary is active).
		val hasEligible = continuationQueue.isNotEmpty()
			|| primaryQueue.isNotEmpty()
			|| (currentPrimaryTaskId == 0L && backgroundQueue.isNotEmpty())

		if (!hasEligible)
			return

		if (!isForwarding.compareAndSet(expect = false, update = true))
			return

		when (val retryNext = pickNext()) {
			null -> isForwarding.value = false
			else -> runOnForwardDispatcher(task = retryNext.task, coroutineContext = retryNext.coroutineContext, block = retryNext.block)
		}
	}


	/**
	 * Launches [block] as a background task.
	 *
	 * The task runs only when no primary task is active and yields as soon as a new primary task
	 * becomes ready.
	 */
	// FIXME non-suspend
	suspend fun launchBackgroundTask(label: String, block: suspend CoroutineScope.() -> Unit): Job {
		val parentTask = currentCoroutineContext()[Task]
		val parentId = if (parentTask?.type == TaskType.primary) parentTask.id else 0L

		return launchTask(label = label, parentId = parentId, type = TaskType.background, block = block)
	}


	/**
	 * Launches [block] as a primary task.
	 *
	 * The task runs to completion before any other primary task or background task may start,
	 * including across suspension points. Multiple primary tasks are serialized in arrival order.
	 */
	fun launchPrimaryTask(label: String, block: suspend CoroutineScope.() -> Unit): Job =
		launchTask(label = label, parentId = 0L, type = TaskType.primary, block = block)


	private fun launchTask(
		label: String,
		parentId: Long,
		type: TaskType,
		block: suspend CoroutineScope.() -> Unit,
	): Job {
		val taskScope = checkNotNull(this.taskScope) { "Not started." }
		val id = nextTaskId.getAndIncrement()
		val rootId = if (parentId != 0L) parentId else id

		val task = Task(
			dispatcher = this,
			id = id,
			label = label,
			parentId = parentId,
			queuedMark = timeSource.markNow(),
			rootId = rootId,
			type = type,
		)
		monitor?.taskCreated(task)

		val coroutineContext =
			if (CoroutineDebugMode.isEnabled) task + CoroutineName("RaptorService[$serviceName]/tasks/${type.name}#${task.id}/$label")
			else task

		val job = taskScope.launch(coroutineContext) {
			monitor?.taskStarted(task)

			block()
		}

		job.invokeOnCompletion { taskCompleted(task) }

		return job
	}


	private fun pickNext(): Dispatch? {
		if (currentPrimaryTaskId != 0L)
			return continuationQueue.poll()

		val primaryDispatch = primaryQueue.poll()
			?: return backgroundQueue.poll()

		currentPrimaryTaskId = primaryDispatch.task.id

		return primaryDispatch
	}


	private fun queueForTask(task: Task): ConcurrentLinkedQueue<Dispatch> =
		when (task.type) {
			TaskType.primary -> when (task.id) {
				currentPrimaryTaskId -> continuationQueue
				else -> primaryQueue
			}

			TaskType.background -> backgroundQueue
		}


	private fun runOnForwardDispatcher(task: Task, coroutineContext: CoroutineContext, block: Runnable) {
		forwardDispatcher.dispatch(coroutineContext) {
			monitor?.dispatchStarted(task, Thread.currentThread())

			try {
				block.run()
			}
			finally {
				monitor?.dispatchCompleted(task)
				forwardNextAcquired()
			}
		}
	}


	suspend fun shutdown(timeout: Duration) {
		val serviceScope = this.serviceScope
			?: return

		val taskScope = this.taskScope
			?: return

		val taskSupervisor = taskScope.coroutineContext.job

		val drained = withTimeoutOrNull(timeout) {
			while (true) {
				val children = taskSupervisor.children.toList()
				if (children.isEmpty()) break
				children.joinAll()
			}
		} != null

		if (!drained) {
			monitor?.logShutdownReport(timeout)
			taskSupervisor.cancelAndJoin()
			continuationQueue.clear()
			primaryQueue.clear()
			backgroundQueue.clear()
		}

		monitor?.stop()
		serviceScope.cancel()
		this.serviceScope = null
		this.taskScope = null
	}


	context(coroutineScope: CoroutineScope)
	fun start() {
		check(this.serviceScope == null) { "Already started." }

		val serviceScope = CoroutineScope(
			coroutineScope.coroutineContext +
				SupervisorJob(parent = coroutineScope.coroutineContext.job) +
				this +
				CoroutineName("RaptorService[$serviceName]")
		)
		this.serviceScope = serviceScope

		this.taskScope = CoroutineScope(
			serviceScope.coroutineContext +
				SupervisorJob(parent = serviceScope.coroutineContext.job) +
				CoroutineName("RaptorService[$serviceName]/tasks")
		)

		monitor?.startMonitoring(serviceScope)
	}


	private fun taskCompleted(task: Task) {
		if (task.type == TaskType.primary && currentPrimaryTaskId == task.id)
			currentPrimaryTaskId = 0L

		monitor?.taskCompleted(task)
	}


	override fun toString(): String =
		"RaptorService[$serviceName]/coroutine-dispatcher"


	private fun tryDispatchFast(task: Task, coroutineContext: CoroutineContext, block: Runnable): Boolean {
		if (!isForwarding.compareAndSet(expect = false, update = true))
			return false

		val isReadyNow = when (task.type) {
			TaskType.primary -> when (task.id) {
				currentPrimaryTaskId -> continuationQueue.isEmpty()
				else -> currentPrimaryTaskId == 0L && primaryQueue.isEmpty()
			}

			TaskType.background -> currentPrimaryTaskId == 0L && primaryQueue.isEmpty() && backgroundQueue.isEmpty()
		}

		if (!isReadyNow) {
			isForwarding.value = false

			return false
		}

		if (task.type == TaskType.primary && currentPrimaryTaskId == 0L)
			currentPrimaryTaskId = task.id

		runOnForwardDispatcher(task = task, coroutineContext = coroutineContext, block = block)

		return true
	}


	private class Dispatch(
		val block: Runnable,
		val coroutineContext: CoroutineContext,
		val task: Task,
	)


	internal class Task(
		val dispatcher: ServiceDispatcher,
		override val id: Long,
		override val label: String,
		override val parentId: Long,
		val queuedMark: TimeMark,
		override val rootId: Long,
		override val type: TaskType,
	) : AbstractCoroutineContextElement(Task), ThreadContextElement<Unit>, ServiceDispatcherTask {

		@Volatile
		var startedMark: TimeMark? = null

		@Volatile
		var thread: Thread? = null

		@Volatile
		var warningSent: Boolean = false


		override fun restoreThreadContext(context: CoroutineContext, oldState: Unit) {}


		override fun updateThreadContext(context: CoroutineContext) {
			if (type == TaskType.primary) {
				val interceptor = context[ContinuationInterceptor]
				if (interceptor !== dispatcher) {
					val message =
						"Primary task #$id '$label' in service '${dispatcher.serviceName}' must not run on a different dispatcher. Found: $interceptor"
					val exception = IllegalStateException(message)
					exception.fillInStackTrace()

					context.job.cancel(message, exception)
				}
			}
		}


		companion object Key : CoroutineContext.Key<Task>
	}


	internal enum class TaskType {
		background,
		primary,
	}
}
