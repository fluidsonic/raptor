package tests

import io.fluidsonic.raptor.*


object CounterFeature : RaptorFeature.Configurable<CounterComponent> {

	override fun RaptorTopLevelConfigurationScope.configure(action: CounterComponent.() -> Unit) {
		componentRegistry.configure(key = CounterComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(CounterComponent.Key, CounterComponent())
	}


	override fun toString() =
		"counter feature"
}
