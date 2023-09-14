package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.RaptorLifecycle.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.slf4j.*


// TODO Needs refactoring.
// TODO Prevent re-use b/c coroutineContext cannot be reused.
internal class DefaultLifecycle(
	override val context: RaptorContext,
	startActions: List<LifecycleAction<RaptorLifecycleStartScope>>,
	stopActions: List<LifecycleAction<RaptorLifecycleStopScope>>,
) : RaptorLifecycle, RaptorLifecycleStartScope, RaptorLifecycleStopScope {

	private var _coroutineContext: CoroutineContext? = null
	private val serviceControllers by lazy {
		context.plugins[RaptorLifecyclePlugin].serviceControllers
	}
	private val stateRef = atomic(State.stopped)

	private val startActions = startActions
		.sortedByDescending { it.priority }

	private val stopActions = stopActions
		.sortedByDescending { it.priority }

	private val logger: Logger by lazy {
		context.di.get()
	}


	override val coroutineContext: CoroutineContext
		get() = checkNotNull(_coroutineContext)


	internal suspend fun createServices() {
		if (serviceControllers.isEmpty())
			return

		val di = context.di

		// TODO Rework.
		coroutineScope {
			serviceControllers.forEach { controller ->
				launch { controller.createIn(this@DefaultLifecycle, di = di, logger = logger) }
			}
		}
	}


	private fun handleException(coroutineContext: CoroutineContext, exception: Throwable) {
		logger.error("Unhandled exception in Raptor lifecycle.\nContext: $coroutineContext", exception)
	}


	@OptIn(ExperimentalTime::class)
	override suspend fun startIn(scope: CoroutineScope) {
		check(stateRef.compareAndSet(expect = State.stopped, update = State.starting)) {
			"Lifecycle can only be started when stopped but it's ${stateRef.value}."
		}

		_coroutineContext = scope.coroutineContext +
			SupervisorJob(parent = scope.coroutineContext.job) +
			CoroutineExceptionHandler(::handleException) +
			CoroutineName("Raptor: lifecycle") // TODO ok?

		for (action in startActions) {
			val duration = measureTime {
				action.block(this)
			}

			logger.debug("Started '${action.label}' in $duration.")
		}

		stateRef.value = State.started
	}


	override val state
		get() = stateRef.value


	internal fun startServices() {
		for (controller in serviceControllers)
			controller.start()
	}


	@OptIn(ExperimentalTime::class)
	override suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) {
			"Lifecycle can only be stopped when started but it's ${stateRef.value}."
		}

		val coroutineContext = checkNotNull(_coroutineContext)

		for (action in stopActions) {
			val duration = measureTime {
				action.block(this)
			}

			logger.debug("Stopped '${action.label}' in $duration.")
		}

		coroutineContext.cancel(CancellationException("Raptor was stopped."))

		_coroutineContext = null
		stateRef.value = State.stopped
	}


	internal suspend fun stopServices() {
		supervisorScope {
			serviceControllers.forEach { controller ->
				launch { controller.stop() }
			}
		}
	}
}
