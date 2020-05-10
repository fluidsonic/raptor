package tests

import io.fluidsonic.raptor.*


object StartableFeature : RaptorFeature.WithRootComponent<StartableComponent> {

	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(StartableComponent.Key, StartableComponent())

		lifecycle.onStart {
			context[StartableRaptorPropertyKey]!!.start()
		}

		lifecycle.onStop {
			context[StartableRaptorPropertyKey]!!.stop()
		}
	}


	override val RaptorFeatureConfigurationStartScope.rootComponentKey: RaptorComponentKey<out StartableComponent>
		get() = StartableComponent.Key
}
