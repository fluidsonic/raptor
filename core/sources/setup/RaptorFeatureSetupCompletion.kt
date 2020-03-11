package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
class RaptorFeatureSetupCompletion internal constructor(
	context: RaptorSetupContext,
	private val kodeinBuilder: Kodein.Builder
) {

	private val startCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	private val stopCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()

	val raptorSetupContext = context


	@Raptor.Dsl3
	fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinBuilder.apply(config)
	}


	@Raptor.Dsl3
	fun onStart(callback: suspend RaptorScope.() -> Unit) {
		startCallbacks += callback
	}


	@Raptor.Dsl3
	fun onStop(callback: suspend RaptorScope.() -> Unit) {
		stopCallbacks += callback
	}
}
