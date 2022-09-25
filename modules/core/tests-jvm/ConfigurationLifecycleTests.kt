package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ConfigurationLifecycleTests {

	@Test
	fun testLifecycleInvocations() {
		val component = ConfigurationLifecycleCounterComponent()

		raptor {
			install(DummyPlugin)

			componentRegistry.register(ConfigurationLifecycleCounterComponent.key, component)
		}

		assertEquals(expected = 1, actual = component.onConfigurationEndedCallCount)
		assertEquals(expected = 1, actual = component.onConfigurationStartedCallCount)
	}
}
