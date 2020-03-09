package io.fluidsonic.raptor

import kotlinx.atomicfu.*
import org.kodein.di.*


internal class RaptorInstance(
	private val config: RaptorConfig
) : RaptorScope, DKodeinAware {

	private var server: RaptorServerInstance? = null
	private val stateRef = atomic(State.initial)

	override val dkodein = Kodein.direct { import(config.kodeinModule) }


	// FIXME call onStarts (begin!), add kodein
	override fun beginTransaction() =
		RaptorTransactionImpl(parentScope = this)


	suspend fun start() {
		check(stateRef.compareAndSet(expect = State.stopped, update = State.starting)) { "Cannot start Raptor unless it's in 'stopped' state." }

		try {
			startServer()
		}
		catch (e: Throwable) {
			stateRef.value = State.stopped

			throw e
		}

		for (callback in config.startCallbacks)
			callback()

		stateRef.value = State.started
	}


	private suspend fun startServer() {
		val config = config.serverConfig ?: return

		val server = RaptorServerInstance(config = config)
		server.start()

		this.server = server
	}


	suspend fun stop() {
		check(stateRef.compareAndSet(expect = State.started, update = State.stopping)) { "Cannot start Raptor unless it's in 'started' state." }

		for (callback in config.stopCallbacks)
			callback()

		try {
			stopServer()
		}
		finally {
			stateRef.value = State.stopped
		}
	}


	private suspend fun stopServer() {
		val server = server ?: return
		this.server = null

		server.stop()
	}


	private enum class State {

		initial,
		started,
		starting,
		stopped,
		stopping
	}
}
