package tests

import io.fluidsonic.raptor.*


class ConfigurationLifecycleCounterComponent : RaptorComponent.Default<ConfigurationLifecycleCounterComponent>() {

	var onConfigurationEndedCallCount = 0
	var onConfigurationStartedCallCount = 0


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		onConfigurationEndedCallCount += 1
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		onConfigurationStartedCallCount += 1
	}


	object Key : RaptorComponentKey<ConfigurationLifecycleCounterComponent> {

		override fun toString() = "configuration lifecycle counter"
	}
}
