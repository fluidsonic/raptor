package tests

import io.fluidsonic.raptor.*


class StartableComponent : RaptorComponent.Default<StartableComponent>() {

	var delayInMilliseconds = 0L


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(StartableRaptorPropertyKey, Startable(delayInMilliseconds = delayInMilliseconds))
	}


	override fun toString() = "startable"


	object Key : RaptorComponentKey<StartableComponent> {

		override fun toString() = "startable"
	}
}
