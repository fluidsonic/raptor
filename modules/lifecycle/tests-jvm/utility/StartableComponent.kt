package tests

import io.fluidsonic.raptor.*


class StartableComponent : RaptorComponent.Base<StartableComponent>(StartablePlugin) {

	var delayInMilliseconds = 0L


	override fun RaptorComponentConfigurationEndScope<StartableComponent>.onConfigurationEnded() {
		propertyRegistry.register(Startable.propertyKey, Startable(delayInMilliseconds = delayInMilliseconds))
	}


	override fun toString() = "startable"
}
