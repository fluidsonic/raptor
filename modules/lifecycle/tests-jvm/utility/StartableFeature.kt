package tests

import io.fluidsonic.raptor.*


private val startableComponentKey = RaptorComponentKey<StartableComponent>("startable")


object StartableFeature : RaptorFeature.Configurable<StartableComponent> {

	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: StartableComponent.() -> Unit) {
		componentRegistry.oneOrRegister(startableComponentKey, ::StartableComponent).action()

		lifecycle.onStart {
			context[Startable.propertyKey]!!.start()
		}

		lifecycle.onStop {
			context[Startable.propertyKey]!!.stop()
		}
	}
}
