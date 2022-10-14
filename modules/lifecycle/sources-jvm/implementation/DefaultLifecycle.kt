package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.lifecycle.RaptorLifecycle.*
import kotlin.coroutines.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*


// TODO Prevent re-use b/c coroutineContext cannot be reused.
internal class DefaultLifecycle(
	override val context: RaptorContext,
	startActions: List<LifecycleAction<RaptorLifecycleStartScope>>,
	stopActions: List<LifecycleAction<RaptorLifecycleStopScope>>,
) : RaptorLifecycle, RaptorLifecycleStartScope, RaptorLifecycleStopScope {

	private var _coroutineContext: CoroutineContext? = null
	private val stateRef = atomic(State.stopped)

	private val startActions = startActions
		.sortedByDescending { it.priority }
		.map { it.block }

	private val stopActions = stopActions
		.sortedByDescending { it.priority }
		.map { it.block }


	override val coroutineContext: CoroutineContext
		get() = checkNotNull(_coroutineContext)


	override suspend fun startIn(scope: CoroutineScope) {
		check(stateRef.compareAndSet(expect = State.stopped, update = State.starting)) {
			"Lifecycle can only be started when stopped but it's ${stateRef.value}."
		}

		_coroutineContext = scope.coroutineContext + Job(parent = scope.coroutineContext[Job]) + CoroutineName("Raptor: lifecycle") // TODO ok?

		for (action in startActions)
			action()

		stateRef.value = State.started
	}


	override val state
		get() = stateRef.value


	override suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) {
			"Lifecycle can only be stopped when started but it's ${stateRef.value}."
		}

		val coroutineContext = checkNotNull(_coroutineContext)

		for (action in stopActions)
			action()

		coroutineContext.cancel(CancellationException("Raptor was stopped."))

		_coroutineContext = null
		stateRef.value = State.stopped
	}
}
