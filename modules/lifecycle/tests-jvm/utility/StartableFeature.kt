package tests

import io.fluidsonic.raptor.*


object StartableFeature : RaptorFeature.Configurable<StartableComponent> {

	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: StartableComponent.() -> Unit) {
		componentRegistry.oneOrRegister(StartableComponent.Key, ::StartableComponent).action()

		lifecycle.onStart {
			context[StartableRaptorPropertyKey]!!.start()
		}

		lifecycle.onStop {
			context[StartableRaptorPropertyKey]!!.stop()
		}
	}
}
