package tests

import io.fluidsonic.raptor.*


object CounterFeature : RaptorFeature.WithRootComponent<CounterComponent> {

	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(CounterComponent.Key, CounterComponent())
	}


	override val RaptorFeatureConfigurationStartScope.rootComponentKey: RaptorComponentKey<out CounterComponent>
		get() = CounterComponent.Key


	override fun toString() =
		"counter feature"
}
