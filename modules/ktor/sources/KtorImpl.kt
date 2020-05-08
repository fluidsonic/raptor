package io.fluidsonic.raptor

import kotlinx.atomicfu.*


internal class KtorImpl(
	config: KtorConfig,
	parentContext: RaptorContext
) {

	private val servers = config.servers.map { config ->
		KtorServerImpl(
			config = config,
			parentContext = parentContext
		)
	}
	private val stateRef = atomic(State.initial)


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.initial, update = State.starting)) { "Cannot start Ktor unless it's in 'initial' state." }

		for (server in servers)
			server.start()

		stateRef.value = State.started
	}


	suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Ktor unless it's in 'started' state." }

		for (server in servers)
			server.stop()

		stateRef.value = State.stopped
	}


	private enum class State {

		initial,
		started,
		starting,
		stopped,
		stopping
	}
}
