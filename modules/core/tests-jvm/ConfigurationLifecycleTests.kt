package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ConfigurationLifecycleTests {

	@Test
	fun testLifecycleInvocations() {
		val component = ConfigurationLifecycleCounterComponent()

		raptor {
			componentRegistry.register(ConfigurationLifecycleCounterComponent.key, component)
		}

		assertEquals(expected = 1, actual = component.onConfigurationEndedCallCount)
		assertEquals(expected = 1, actual = component.onConfigurationStartedCallCount)
	}


	@Test
	fun testConfigurationAfterEndFails() {
		raptor {
			install(TextCollectionFeature)

			val textCollection = componentRegistry.all(TextCollectionComponent.key)

			install(object : RaptorFeature {

				override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
					assertFails {
						componentRegistry.all(TextCollectionComponent.key)
					}

					assertFails {
						componentRegistry.register(DummyComponent.key, DummyComponent())
					}

					assertFails {
						textCollection.all {}
					}
				}


				override fun RaptorFeatureScope.installed() {}
			})
		}
	}
}
