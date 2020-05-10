package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ConfigurationTests {

	@Test
	fun testMultipleConfigurations() {
		val configurationA = SingleValueConfiguration(path = "a", value = "a")
		val configurationB = SingleValueConfiguration(path = "b", value = "b")
		val configurationC = SingleValueConfiguration(path = "a", value = "c")

		val raptor = raptor {
			install(RaptorConfigurationFeature)

			configuration(configurationA)
			configuration(configurationB)
			configuration(configurationC)
		}

		assertSame(expected = "a", actual = raptor.configuration.value("a").string())
		assertSame(expected = "b", actual = raptor.configuration.value("b").string())
	}


	@Test
	fun testSingleConfiguration() {
		val configuration = SingleValueConfiguration(path = "foo", value = "bar")

		val raptor = raptor {
			install(RaptorConfigurationFeature)

			configuration(configuration)
		}

		assertSame(expected = "bar", actual = raptor.configuration.value("foo").string())
	}


	@Test
	fun testZeroConfigurations() {
		val raptor = raptor {
			install(RaptorConfigurationFeature)
		}

		assertSame(expected = EmptyRaptorConfiguration, actual = raptor.configuration)
	}


	@Test
	fun testConfigurationWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "You must install RaptorConfigurationFeature for enabling configuration functionality.",
			actual = assertFails {
				raptor.configuration
			}.message
		)
	}
}
