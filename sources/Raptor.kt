package io.fluidsonic.raptor


interface Raptor {

	val state: State

	suspend fun start()
	suspend fun stop()


	enum class State {

		started,
		starting,
		stopped,
		stopping
	}
}


fun raptor(configure: RaptorConfigScope.() -> Unit): Raptor =
	RaptorImpl(config = RaptorConfigScopeImpl().apply(configure).build())
