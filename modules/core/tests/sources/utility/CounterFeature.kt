package tests

import io.fluidsonic.raptor.*


object CounterFeature : RaptorConfigurableFeature<CounterComponent> {

	override fun RaptorFeatureFinalizationScope.finalizeConfigurable() {
		propertyRegistry.register(CountRaptorPropertyKey, componentRegistry.one(CounterComponent.Key).finalize())
	}


	override fun RaptorFeatureInstallationScope.installConfigurable(): RaptorComponentKey<CounterComponent> {
		componentRegistry.register(CounterComponent.Key, CounterComponent())

		return CounterComponent.Key
	}


	override fun toString() =
		"counter feature"
}
