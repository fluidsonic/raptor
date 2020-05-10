package io.fluidsonic.raptor


interface RaptorLifecycle {

	val state: State

	suspend fun start()
	suspend fun stop()


	enum class State {

		started,
		starting,
		stopped,
		stopping
	}


	companion object
}
