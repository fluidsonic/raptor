package io.fluidsonic.raptor

import io.fluidsonic.raptor.Raptor.*
import kotlinx.atomicfu.*


internal class RaptorImpl(
	private val config: RaptorConfig
) : Raptor {

	private val stateRef = atomic(State.initial)

	override val context = RaptorContextImpl(
		kodeinModule = config.kodeinModule
	)


	override suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start Raptor unless it's in 'stopped' state." }

		with(context) {
			for (callback in this@DefaultRaptor.config.startCallbacks) // FIXME get rid of this@RaptorImpl.
				callback()
		}

		stateRef.value = State.started
	}


	override val state
		get() = stateRef.value


	override suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Raptor unless it's in 'started' state." }

		with(context) {
			for (callback in this@DefaultRaptor.config.stopCallbacks) // FIXME get rid of this@RaptorImpl.
				callback()
		}

		stateRef.value = State.stopped
	}
}
