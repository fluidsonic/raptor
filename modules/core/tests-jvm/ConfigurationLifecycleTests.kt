package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ConfigurationLifecycleTests {

	@Test
	fun testLifecycleInvocations() {
		val component = ConfigurationLifecycleCounterComponent()

		raptor {
			componentRegistry.register(ConfigurationLifecycleCounterComponent.Key, component)
		}

		assertEquals(expected = 1, actual = component.onConfigurationEndedCallCount)
		assertEquals(expected = 1, actual = component.onConfigurationStartedCallCount)
	}


	@Test
	fun testConfigurationAfterEndFails() {
		raptor {
			install(TextCollectionFeature)

			val textCollection = componentRegistry.configure(TextCollectionComponent.Key)

			install(object : RaptorFeature {

				override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
					assertFails {
						componentRegistry.configure(TextCollectionComponent.Key)
					}

					assertFails {
						componentRegistry.register(DummyComponent.Key, DummyComponent())
					}

					assertFails {
						textCollection {}
					}
				}


				override fun RaptorFeatureConfigurationScope.beginConfiguration() = Unit
			})
		}
	}
}
