package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
class RaptorFeatureSetupCompletion internal constructor(
	val componentRegistry: RaptorComponentRegistry
) {

	internal val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()
	internal val startCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	internal val stopCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()


	@Raptor.Dsl3
	fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
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
