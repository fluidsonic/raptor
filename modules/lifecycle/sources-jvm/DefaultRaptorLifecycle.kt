package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorLifecycle.*
import kotlin.coroutines.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*


// FIXME prevent reuse b/c coroutineContext cannot be reused
internal class DefaultRaptorLifecycle(
	override val context: RaptorContext,
	private val startActions: List<suspend RaptorLifecycleStartScope.() -> Unit>,
	private val stopActions: List<suspend RaptorLifecycleStopScope.() -> Unit>,
) : RaptorLifecycle, RaptorLifecycleStartScope, RaptorLifecycleStopScope {

	private var _coroutineContext: CoroutineContext? = null
	private val stateRef = atomic(State.stopped)

	override val coroutineContext: CoroutineContext
		get() = checkNotNull(_coroutineContext)


	override suspend fun startIn(scope: CoroutineScope) {
		check(stateRef.compareAndSet(expect = State.stopped, update = State.starting)) {
			"Lifecycle can only be started when stopped but it's ${stateRef.value}."
		}

		_coroutineContext = scope.coroutineContext + Job(parent = scope.coroutineContext[Job]) + CoroutineName("Raptor: lifecycle") // FIXME ok?

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


	object PropertyKey : RaptorPropertyKey<DefaultRaptorLifecycle> {

		override fun toString() = "lifecycle"
	}
}
