package tests

import io.fluidsonic.raptor.*


object StartableFeature : RaptorFeature.Configurable<StartableComponent> {

	override fun RaptorTopLevelConfigurationScope.configure(action: StartableComponent.() -> Unit) {
		componentRegistry.configure(key = StartableComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(StartableComponent.Key, StartableComponent())

		lifecycle.onStart {
			context[StartableRaptorPropertyKey]!!.start()
		}

		lifecycle.onStop {
			context[StartableRaptorPropertyKey]!!.stop()
		}
	}
}
