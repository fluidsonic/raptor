package io.fluidsonic.raptor

import kotlinx.atomicfu.*


// FIXME prevent multi-start in feature with clear message
internal class RaptorKtorImpl(
	configuration: KtorConfiguration,
	context: RaptorContext,
) : RaptorKtor {

	private val stateRef = atomic(State.initial)

	override val servers = configuration.servers.map { configuration ->
		RaptorKtorServerImpl(
			configuration = configuration,
			parentContext = context
		)
	}


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start Ktor unless it's in 'initial' state." }

		for (server in servers)
			server.start()

		stateRef.value = State.started
	}


	suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot stop Ktor unless it's in 'started' state." }

		for (server in servers)
			server.stop()

		stateRef.value = State.stopped
	}


	object PropertyKey : RaptorPropertyKey<RaptorKtorImpl> {

		override fun toString() = "ktor"
	}


	private enum class State {

		initial,
		started,
		starting,
		stopped,
		stopping
	}
}
