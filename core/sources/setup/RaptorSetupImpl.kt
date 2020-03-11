package io.fluidsonic.raptor

import org.kodein.di.*


internal class RaptorSetupImpl : RaptorSetup {

	private val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()
	private val startCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()
	private val stopCallbacks: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()

	override val raptorComponentRegistry = RaptorComponentRegistry()


	init {
		raptorComponentRegistry.register<RaptorFeatureComponent>(RaptorFeatureSetupImpl(
			context = raptorComponentRegistry
		))
	}


	fun complete() = RaptorConfig(
		kodeinModule = Kodein.Module("raptor") {
			for (config in kodeinConfigs)
				config()
		},
		startCallbacks = startCallbacks,
		stopCallbacks = stopCallbacks
	)


	// FIXME duplicates
	override fun install(feature: RaptorFeature) {
		with(feature) {
			setup()
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}


	override fun onStart(callback: suspend RaptorScope.() -> Unit) {
		startCallbacks += callback
	}


	override fun onStop(callback: suspend RaptorScope.() -> Unit) {
		stopCallbacks += callback
	}
}
