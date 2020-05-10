package tests

import io.fluidsonic.raptor.*


object StartableFeature : RaptorConfigurableFeature<StartableComponent> {

	override fun RaptorFeatureFinalizationScope.finalizeConfigurable() {
		propertyRegistry.register(StartableRaptorPropertyKey, componentRegistry.one(StartableComponent.Key).finalize())
	}


	override fun RaptorFeatureInstallationScope.installConfigurable(): RaptorComponentKey<StartableComponent> {
		componentRegistry.register(StartableComponent.Key, StartableComponent())

		lifecycle.onStart {
			context[StartableRaptorPropertyKey]!!.start()
		}

		lifecycle.onStop {
			context[StartableRaptorPropertyKey]!!.stop()
		}

		return StartableComponent.Key
	}
}
