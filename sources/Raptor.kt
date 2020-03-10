package io.fluidsonic.raptor


interface Raptor {

	val state: State

	suspend fun start()
	suspend fun stop()


	enum class State {

		initial,
		started,
		starting,
		stopped,
		stopping
	}
}


fun raptor(configure: RaptorSetup.() -> Unit): Raptor =
	RaptorImpl(config = RaptorSetupImpl().apply(configure).complete())
