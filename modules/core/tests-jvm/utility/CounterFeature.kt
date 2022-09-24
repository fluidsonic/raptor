package tests

import io.fluidsonic.raptor.*


object CounterFeature : RaptorFeature.Configurable<CounterComponent> {

	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: CounterComponent.() -> Unit) {
		componentRegistry.oneOrRegister(CounterComponent.key, ::CounterComponent).action()
	}


	override fun toString() =
		"counter feature"
}
