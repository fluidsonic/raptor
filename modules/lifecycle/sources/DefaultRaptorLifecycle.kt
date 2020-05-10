package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorLifecycle.*
import kotlinx.atomicfu.*


internal class DefaultRaptorLifecycle(
	private val onStartActions: List<suspend RaptorLifecycleStartScope.() -> Unit>,
	private val onStopActions: List<suspend RaptorLifecycleStopScope.() -> Unit>
) : RaptorLifecycle, RaptorLifecycleStartScope, RaptorLifecycleStopScope {

	private val stateRef = atomic(State.stopped)

	override lateinit var context: RaptorContext


	override suspend fun start() {
		check(stateRef.compareAndSet(expect = State.stopped, update = State.starting)) {
			"Lifecycle can only be started when stopped but it's ${stateRef.value}."
		}

		for (action in onStartActions)
			action()

		stateRef.value = State.started
	}


	override val state
		get() = stateRef.value


	override suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) {
			"Lifecycle can only be stopped when started but it's ${stateRef.value}."
		}

		for (action in onStopActions)
			action()

		stateRef.value = State.stopped
	}


	object PropertyKey : RaptorPropertyKey<DefaultRaptorLifecycle> {

		override fun toString() = "lifecycle"
	}
}
