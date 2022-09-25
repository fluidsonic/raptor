package tests

import io.fluidsonic.raptor.*


class ConfigurationLifecycleCounterComponent : RaptorComponent.Base<ConfigurationLifecycleCounterComponent>(DummyPlugin) {

	var onConfigurationEndedCallCount = 0
	var onConfigurationStartedCallCount = 0


	override fun RaptorComponentConfigurationEndScope<ConfigurationLifecycleCounterComponent>.onConfigurationEnded() {
		onConfigurationEndedCallCount += 1
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		onConfigurationStartedCallCount += 1
	}


	companion object {

		val key = RaptorComponentKey<ConfigurationLifecycleCounterComponent>("configuration lifecycle counter")
	}
}
