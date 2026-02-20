package io.fluidsonic.raptor.service2

import java.util.concurrent.*
import kotlin.time.*
import kotlin.time.Duration.Companion.nanoseconds
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.slf4j.*


internal data class ServiceDispatcherStatistics(
	val backgroundTasks: DurationStatistics,
	val primaryTasks: DurationStatistics,
	val tasks: List<TaskInfo>,
) {

	internal data class DurationStatistics(
		val average: Duration?,
		val max: Duration?,
		val median: Duration?,
		val min: Duration?,
		val totalCompleted: Long,
		val totalCreated: Long,
	) {

		val totalPending: Long get() = totalCreated - totalCompleted
	}


	internal data class TaskInfo(
		override val id: Long,
		override val label: String,
		override val parentId: Long,
		override val rootId: Long,
		override val type: ServiceDispatcher.TaskType,
		val queuedDuration: Duration,
		val runningDuration: Duration?,
		val threadName: String?,
	) : ServiceDispatcherTask
}


internal class ServiceDispatcherMonitor(
	private val configuration: Configuration,
	private val logger: Logger,
	private val serviceName: String,
	private val timeSource: TimeSource,
) {

	private val backgroundDurations = DurationTracker(configuration.durationHistorySize)
	private val backgroundTasksCompleted = atomic(0L)
	private val backgroundTasksCreated = atomic(0L)
	private val primaryDurations = DurationTracker(configuration.durationHistorySize)
	private val primaryTasksCompleted = atomic(0L)
	private val primaryTasksCreated = atomic(0L)
	private val tasks = ConcurrentHashMap<Long, ServiceDispatcher.Task>()

	@Volatile
	private var currentDispatchStartMark: TimeMark? = null

	@Volatile
	private var currentDispatchTaskId: Long = 0L

	@Volatile
	private var currentDispatchThread: Thread? = null

	@Volatile
	private var currentDispatchWarningSent: Boolean = false

	private var monitorJob: Job? = null
	private val signal = Channel<Unit>(Channel.CONFLATED)

	@Volatile
	private var stopped = false


	private fun appendTaskLine(builder: StringBuilder, task: ServiceDispatcher.Task, indent: String) {
		val queuedDuration = task.queuedMark.elapsedNow()
		val startedMark = task.startedMark
		val runningDuration = startedMark?.elapsedNow()
		val thread = task.thread

		builder.append(indent)
		builder.append("${task.type} #${task.id} '${task.label}'")
		builder.append("  queued ${formatDuration(queuedDuration)} ago")

		if (runningDuration != null)
			builder.append(", running for ${formatDuration(runningDuration)}")

		when {
			thread != null -> builder.append("  [Thread: ${thread.name}]")
			runningDuration != null -> builder.append("  [suspended]")
			else -> builder.append("  [queued]")
		}

		builder.append("\n")
	}


	private fun checkDispatchDuration() {
		val startMark = currentDispatchStartMark ?: return
		if (currentDispatchWarningSent) return

		val taskId = currentDispatchTaskId
		val task = tasks[taskId] ?: return

		val limit = when (task.type) {
			ServiceDispatcher.TaskType.primary -> configuration.primaryDispatchDurationLimit
			ServiceDispatcher.TaskType.background -> configuration.backgroundDispatchDurationLimit
		}

		if (limit.isInfinite()) return

		val elapsed = startMark.elapsedNow()
		if (elapsed < limit) return

		currentDispatchWarningSent = true

		val thread = currentDispatchThread

		val message = buildString {
			append(
				"Slow dispatch in service '$serviceName' (${task.type} #${task.id} '${task.label}'): " +
					"running for ${formatDuration(elapsed)} (limit: ${formatDuration(limit)})."
			)
			if (thread != null) {
				append("\n  Thread '${thread.name}':\n")
				append(formatStackTrace(thread))
			}
			append("\n")
			append(formatPendingTasks())
		}

		logger.warn(message)
	}


	private fun checkPrimaryTaskDurations() {
		val limit = configuration.primaryTaskDurationLimit
		if (limit.isInfinite()) return

		for (task in tasks.values) {
			if (task.type != ServiceDispatcher.TaskType.primary) continue
			if (task.warningSent) continue

			val startedMark = task.startedMark ?: continue
			val elapsed = startedMark.elapsedNow()
			if (elapsed < limit) continue

			task.warningSent = true
			val thread = task.thread

			val message = buildString {
				append(
					"Slow primary task in service '$serviceName' (#${task.id} '${task.label}'): " +
						"running for ${formatDuration(elapsed)} (limit: ${formatDuration(limit)})."
				)
				if (thread != null) {
					append("\n  Thread '${thread.name}':\n")
					append(formatStackTrace(thread))
				}
				else {
					append("\n  Task is suspended, not currently on any thread.")
				}
				append("\n")
				append(formatPendingTasks())
			}

			logger.warn(message)
		}
	}


	private fun calculateNextCheckDelay(): Duration? {
		var earliest: Duration? = null

		val dispatchStart = currentDispatchStartMark

		if (dispatchStart != null && !currentDispatchWarningSent) {
			val taskId = currentDispatchTaskId
			val task = tasks[taskId]

			if (task != null) {
				val limit = when (task.type) {
					ServiceDispatcher.TaskType.primary -> configuration.primaryDispatchDurationLimit
					ServiceDispatcher.TaskType.background -> configuration.backgroundDispatchDurationLimit
				}

				if (!limit.isInfinite()) {
					val remaining = limit - dispatchStart.elapsedNow()

					if (remaining > Duration.ZERO)
						earliest = remaining
				}
			}
		}

		val taskLimit = configuration.primaryTaskDurationLimit

		if (!taskLimit.isInfinite()) {
			for (task in tasks.values) {
				if (task.type != ServiceDispatcher.TaskType.primary) continue
				if (task.warningSent) continue

				val startedMark = task.startedMark ?: continue
				val remaining = taskLimit - startedMark.elapsedNow()

				if (remaining > Duration.ZERO)
					earliest = earliest?.let { minOf(it, remaining) } ?: remaining
			}
		}

		return earliest
	}


	fun dispatchCompleted(task: ServiceDispatcher.Task) {
		task.thread = null
		currentDispatchStartMark = null
		currentDispatchTaskId = 0L
		currentDispatchThread = null
		currentDispatchWarningSent = false
		notifyMonitor()
	}


	fun dispatchStarted(task: ServiceDispatcher.Task, thread: Thread) {
		task.thread = thread
		currentDispatchStartMark = timeSource.markNow()
		currentDispatchTaskId = task.id
		currentDispatchThread = thread
		currentDispatchWarningSent = false
		notifyMonitor()
	}


	private fun formatDuration(duration: Duration): String {
		val milliseconds = duration.inWholeMilliseconds

		return when {
			milliseconds < 1000 -> "${milliseconds}ms"
			milliseconds < 60_000 -> "${"%.1f".format(milliseconds / 1000.0)}s"
			else -> "${milliseconds / 60_000}m ${(milliseconds % 60_000) / 1000}s"
		}
	}


	private fun formatPendingTasks(): String {
		val snapshot = tasks.values.toList()
		val builder = StringBuilder()
		builder.append("Pending tasks for service '$serviceName':\n")

		val roots = snapshot.filter { it.id == it.rootId }.sortedBy { it.id }
		val childrenByRootId = snapshot.filter { it.id != it.rootId }.groupBy { it.rootId }

		for (root in roots) {
			appendTaskLine(builder, root, indent = "  ")
			val children = childrenByRootId[root.id]?.sortedBy { it.id } ?: emptyList()
			for (child in children)
				appendTaskLine(builder, child, indent = "    ")
		}

		return builder.toString().trimEnd()
	}


	private fun formatStackTrace(thread: Thread): String =
		thread.stackTrace.joinToString("\n") { "    at $it" }


	fun logShutdownReport(timeout: Duration) {
		val primaryStats = primaryDurations.snapshot(primaryTasksCreated.value)
		val backgroundStats = backgroundDurations.snapshot(backgroundTasksCreated.value)
		val pendingTasksText = formatPendingTasks()
		val dispatchThread = currentDispatchThread

		val message = buildString {
			append("Service '$serviceName' shutdown timed out after ${formatDuration(timeout)}.\n")
			append("  ${primaryStats.totalPending} primary tasks pending (${primaryStats.totalCreated} created, ${primaryStats.totalCompleted} completed)\n")
			append("  ${backgroundStats.totalPending} background tasks pending (${backgroundStats.totalCreated} created, ${backgroundStats.totalCompleted} completed)\n")

			val primaryMin = primaryStats.min
			val primaryMax = primaryStats.max

			if (primaryMin != null || primaryMax != null)
				append(
					"  Duration stats — primary: " +
						"min=${primaryMin?.let { formatDuration(it) } ?: "n/a"}, " +
						"max=${primaryMax?.let { formatDuration(it) } ?: "n/a"}, " +
						"avg=${primaryStats.average?.let { formatDuration(it) } ?: "n/a"}, " +
						"median=${primaryStats.median?.let { formatDuration(it) } ?: "n/a"}\n"
				)

			val backgroundMin = backgroundStats.min
			val backgroundMax = backgroundStats.max

			if (backgroundMin != null || backgroundMax != null)
				append(
					"  Duration stats — background: " +
						"min=${backgroundMin?.let { formatDuration(it) } ?: "n/a"}, " +
						"max=${backgroundMax?.let { formatDuration(it) } ?: "n/a"}, " +
						"avg=${backgroundStats.average?.let { formatDuration(it) } ?: "n/a"}, " +
						"median=${backgroundStats.median?.let { formatDuration(it) } ?: "n/a"}\n"
				)

			if (dispatchThread != null) {
				append("  In-flight thread '${dispatchThread.name}':\n")
				append(formatStackTrace(dispatchThread))
				append("\n")
			}

			append(pendingTasksText)
		}

		logger.error(message)
	}


	private fun monitorTick() {
		checkDispatchDuration()
		checkPrimaryTaskDurations()
	}


	private fun notifyMonitor() {
		signal.trySend(Unit)
	}


	fun startMonitoring(scope: CoroutineScope) {
		val hasAnyLimit =
			!configuration.primaryDispatchDurationLimit.isInfinite() ||
				!configuration.backgroundDispatchDurationLimit.isInfinite() ||
				!configuration.primaryTaskDurationLimit.isInfinite() ||
				configuration.primaryTaskQueueLimit != Int.MAX_VALUE ||
				configuration.backgroundTaskQueueLimit != Int.MAX_VALUE

		if (!hasAnyLimit) return

		monitorJob = scope.launch(Dispatchers.IO) {
			while (isActive) {
				val nextDelay = calculateNextCheckDelay()

				if (nextDelay != null)
					withTimeoutOrNull(nextDelay) { signal.receive() }
				else
					signal.receive()

				monitorTick()
			}
		}
	}


	fun statistics(): ServiceDispatcherStatistics {
		val snapshot = tasks.values.toList()

		val taskSnapshots = snapshot.map { task ->
			ServiceDispatcherStatistics.TaskInfo(
				id = task.id,
				label = task.label,
				parentId = task.parentId,
				queuedDuration = task.queuedMark.elapsedNow(),
				rootId = task.rootId,
				runningDuration = task.startedMark?.elapsedNow(),
				threadName = task.thread?.name,
				type = task.type,
			)
		}

		return ServiceDispatcherStatistics(
			backgroundTasks = backgroundDurations.snapshot(backgroundTasksCreated.value),
			primaryTasks = primaryDurations.snapshot(primaryTasksCreated.value),
			tasks = taskSnapshots,
		)
	}


	fun stop() {
		stopped = true
		monitorJob?.cancel()
		monitorJob = null
	}


	fun taskCompleted(task: ServiceDispatcher.Task) {
		check(!stopped) { "Monitor is stopped — taskCompleted() called after stop()." }

		val startedMark = task.startedMark

		if (startedMark != null) {
			val duration = startedMark.elapsedNow()

			when (task.type) {
				ServiceDispatcher.TaskType.primary -> primaryDurations.record(duration)
				ServiceDispatcher.TaskType.background -> backgroundDurations.record(duration)
			}
		}

		when (task.type) {
			ServiceDispatcher.TaskType.primary -> primaryTasksCompleted.incrementAndGet()
			ServiceDispatcher.TaskType.background -> backgroundTasksCompleted.incrementAndGet()
		}

		tasks.remove(task.id)
		notifyMonitor()
	}


	fun taskCreated(task: ServiceDispatcher.Task) {
		when (task.type) {
			ServiceDispatcher.TaskType.primary -> primaryTasksCreated.incrementAndGet()
			ServiceDispatcher.TaskType.background -> backgroundTasksCreated.incrementAndGet()
		}

		tasks[task.id] = task
		warnIfQueueDepthExceeded(task.type)
	}


	fun taskStarted(task: ServiceDispatcher.Task) {
		task.startedMark = timeSource.markNow()
		notifyMonitor()
	}


	private fun warnIfQueueDepthExceeded(type: ServiceDispatcher.TaskType) {
		val (limit, pending) = when (type) {
			ServiceDispatcher.TaskType.primary ->
				configuration.primaryTaskQueueLimit to (primaryTasksCreated.value - primaryTasksCompleted.value)

			ServiceDispatcher.TaskType.background ->
				configuration.backgroundTaskQueueLimit to (backgroundTasksCreated.value - backgroundTasksCompleted.value)
		}

		if (limit == Int.MAX_VALUE || pending <= limit) return

		logger.warn(
			"Queue depth exceeded for $type tasks in service '$serviceName': " +
				"$pending pending (limit: $limit).\n${formatPendingTasks()}"
		)
	}


	internal data class Configuration(
		val backgroundDispatchDurationLimit: Duration = Duration.INFINITE,
		val backgroundTaskQueueLimit: Int = Int.MAX_VALUE,
		val durationHistorySize: Int = 1_000,
		val primaryDispatchDurationLimit: Duration = Duration.INFINITE,
		val primaryTaskDurationLimit: Duration = Duration.INFINITE,
		val primaryTaskQueueLimit: Int = Int.MAX_VALUE,
	)


	private class DurationTracker(private val historySize: Int) {

		private val allTimeMaxNanos = atomic(Long.MIN_VALUE)
		private val allTimeMinNanos = atomic(Long.MAX_VALUE)
		private val buffer = arrayOfNulls<Duration>(historySize)
		private val count = atomic(0L)
		private val index = atomic(0)


		fun record(duration: Duration) {
			val nanos = duration.inWholeNanoseconds

			while (true) {
				val current = index.value
				val next = (current + 1) % historySize

				if (index.compareAndSet(current, next)) {
					buffer[current] = duration
					break
				}
			}

			allTimeMaxNanos.update { maxOf(it, nanos) }
			allTimeMinNanos.update { minOf(it, nanos) }
			count.incrementAndGet()
		}


		fun snapshot(totalCreated: Long): ServiceDispatcherStatistics.DurationStatistics {
			val completed = count.value
			val entries = buffer.filterNotNull()
			val sorted = entries.sorted()

			val average = when {
				entries.isEmpty() -> null
				else -> entries.fold(Duration.ZERO) { accumulator, duration -> accumulator + duration } / entries.size
			}

			val median = when {
				sorted.isEmpty() -> null
				else -> sorted[sorted.size / 2]
			}

			val maxNanos = allTimeMaxNanos.value
			val max = when (maxNanos) {
				Long.MIN_VALUE -> null
				else -> maxNanos.nanoseconds
			}

			val minNanos = allTimeMinNanos.value
			val min = when (minNanos) {
				Long.MAX_VALUE -> null
				else -> minNanos.nanoseconds
			}

			return ServiceDispatcherStatistics.DurationStatistics(
				average = average,
				max = max,
				median = median,
				min = min,
				totalCompleted = completed,
				totalCreated = totalCreated,
			)
		}
	}
}
