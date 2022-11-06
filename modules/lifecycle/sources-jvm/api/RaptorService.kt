package io.fluidsonic.raptor.lifecycle

import kotlin.coroutines.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.slf4j.*


public abstract class RaptorService {

	private var instance: Instance? = null


	internal suspend fun createIn(scope: CoroutineScope, logger: Logger, name: String) {
		check(instance == null)

		// FIXME hack
		val instanceDeferred = CompletableDeferred<Instance>()

		scope.launch(CoroutineName("RaptorService '$name'")) {
			val completionSignal = CompletableDeferred<Unit>()

			try {
				supervisorScope {
					instanceDeferred.complete(Instance(
						completionSignal = completionSignal,
						coroutineContext = coroutineContext,
						logger = logger,
						name = name,
					))
				}
			}
			finally {
				completionSignal.complete(Unit)
			}
		}

		val instance = instanceDeferred.await()
		this.instance = instance

		instance.create()
	}


	private fun createException(message: String, cause: Throwable? = null) =
		RaptorServiceException(service = this, message = "RaptorService '$name': $message", cause = cause)


	private fun error(message: String, cause: Throwable? = null): Nothing {
		throw createException(message = message, cause = cause)
	}


	private val name: String
		get() = instance?.name ?: this::class.qualifiedName ?: "<anonymous class>"


	public val scope: CoroutineScope
		get() = instance ?: error("Cannot access `scope` before the service was started.")


	internal fun start() {
		checkNotNull(instance).start()
	}


	internal suspend fun stop() {
		checkNotNull(instance).stop()
	}


	protected fun Job.cancelOnStop() {
		val instance = instance ?: error("Cannot call `Job.cancelOnStop()` before the service was created.")
		instance.cancelOnStop(job)
	}


	protected open fun RaptorServiceExceptionScope.exceptionRaised(coroutineContext: CoroutineContext, exception: Throwable) {
		val instance = instance ?: error("Cannot call `exceptionRaised()` directly.")

		instance.defaultExceptionRaised(coroutineContext, exception)
	}


	protected open suspend fun RaptorServiceCreationScope.created() {} // TODO Does this need to be suspended?
	protected open fun RaptorServiceStartScope.started() {}
	protected open suspend fun RaptorServiceStopScope.stopped() {}


	override fun toString(): String =
		"RaptorService '$name'"


	private companion object {

		// TODO Make configurable?
		private val stopCancelTime = 5.minutes // Must be at least `stopChildJobsWarnTime + stopHandlerWarnTime`.
		private val stopChildJobsWarnTime = 30.seconds
		private val stopHandlerWarnTime = 30.seconds
	}


	private inner class Instance(
		private val completionSignal: CompletableDeferred<Unit>,
		coroutineContext: CoroutineContext,
		val logger: Logger,
		val name: String,
	) : CoroutineScope,
		RaptorServiceCreationScope,
		RaptorServiceExceptionScope,
		RaptorServiceStartScope,
		RaptorServiceStopScope {

		private val shutdownStartedSignal = CompletableDeferred<Unit>()
		private val shutdownCompletedSignal = CompletableDeferred<Unit>()
		private var status = atomic(Status.new)

		override val coroutineContext = coroutineContext + CoroutineExceptionHandler(::handleException)


		init {
			launch(CoroutineName("RaptorService '$name': keep-alive")) {
				shutdownCompletedSignal.await()
			}
		}


		fun cancelOnStop(job: Job) {
			val exception = CancellationException("RaptorService '$name': Service is stopping.")

			shutdownStartedSignal.invokeOnCompletion {
				job.cancel(exception)
			}
		}


		suspend fun create() {
			check(status.compareAndSet(expect = Status.new, update = Status.creating))

			try {
				created()
			}
			catch (e: Throwable) {
				check(status.compareAndSet(expect = Status.creating, update = Status.failed))

				coroutineContext.job.cancel("RaptorService '$name': Creating the service failed.", e)

				val exception = when (e) {
					is CancellationException -> CancellationException("RaptorService '$name': Creating the service failed.", e)
					else -> createException("Creating the service failed.", e)
				}

				shutdownStartedSignal.complete(Unit)

				throw exception
			}

			check(status.compareAndSet(expect = Status.creating, update = Status.created))
		}


		fun defaultExceptionRaised(coroutineContext: CoroutineContext, exception: Throwable) {
			logger.error("RaptorService '$name': Child job failed.\nContext: $coroutineContext", exception)
		}


		private fun handleException(coroutineContext: CoroutineContext, exception: Throwable) {
			status.value.let { status ->
				if (status == Status.failed)
					return

				if (status != Status.started)
					error("Unexpected service status '$status' when handling exception.")
			}

			try {
				exceptionRaised(coroutineContext = coroutineContext, exception = exception)
			}
			catch (e: Throwable) {
				if (!status.compareAndSet(expect = Status.started, update = Status.failed))
					return

				if (e.cause !== exception && e.suppressedExceptions.none { it === exception })
					e.addSuppressed(createException(message = "Child job failed.\nContext: $coroutineContext", cause = exception))

				this@Instance.coroutineContext.job.cancel(
					message = "RaptorService '$name': `exceptionRaised()` handler raised another exception. Service will be shut down immediately.",
					cause = e,
				)

				shutdownStartedSignal.complete(Unit)
			}
		}


		fun start() {
			check(status.compareAndSet(expect = Status.created, update = Status.started))

			launch {
				started()
			}
		}


		suspend fun stop() {
			if (status.value == Status.failed)
				return

			check(status.compareAndSet(expect = Status.started, update = Status.stopping))

			shutdownStartedSignal.complete(Unit)

			try {
				withTimeout(stopCancelTime) {
					tryStop()
				}

				check(status.compareAndSet(expect = Status.stopping, update = Status.stopped))
			}
			catch (e: Throwable) {
				if (status.value == Status.failed)
					return

				check(status.compareAndSet(expect = Status.stopping, update = Status.failed))

				when (e) {
					is TimeoutCancellationException ->
						logger.error("RaptorService '$name': Service didn't stop after $stopCancelTime. Will cancel remaining jobs.")

					is CancellationException ->
						throw CancellationException("RaptorService '$name': Stopping the service failed.", e)

					else ->
						logger.error("RaptorService '$name': Stopping the service failed.", e)
				}

				coroutineContext.job.cancelAndJoin()
			}
			finally {
				coroutineContext.job.cancel()
			}
		}


		private suspend fun tryStop() {
			val stoppedWarnJob = scope.launch {
				delay(stopHandlerWarnTime)

				logger.warn("RaptorService '$name': `stopped()` is taking more than $stopHandlerWarnTime. Will keep waiting…")
			}

			withContext(coroutineContext) {
				try {
					stopped()
				}
				finally {
					stoppedWarnJob.cancel()
				}
			}

			try {
				shutdownCompletedSignal.complete(Unit)

				withTimeout(stopChildJobsWarnTime) {
					completionSignal.await()
				}
			}
			catch (e: TimeoutCancellationException) {
				logger.warn("RaptorService '$name': Service still has jobs running $stopChildJobsWarnTime after calling `stopped()`. Will keep waiting…")

				completionSignal.await()
			}
		}
	}


	private enum class Status {

		created,
		creating,
		failed,
		new,
		started,
		stopped,
		stopping,
	}
}
