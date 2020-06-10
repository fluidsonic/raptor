package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ConfigurationTests {

	@Test
	fun testMultipleConfigurations() {
		val configurationA = SingleValueSettings(path = "a", value = "a")
		val configurationB = SingleValueSettings(path = "b", value = "b")
		val configurationC = SingleValueSettings(path = "a", value = "c")

		val raptor = raptor {
			install(RaptorConfigurationFeature)

			configuration.append(configurationA)
			configuration.append(configurationB)
			configuration.append(configurationC)
		}

		assertSame(expected = "c", actual = raptor.configuration.value("a").string())
		assertSame(expected = "b", actual = raptor.configuration.value("b").string())
	}


	@Test
	fun testSingleConfiguration() {
		val configuration = SingleValueSettings(path = "foo", value = "bar")

		val raptor = raptor {
			install(RaptorConfigurationFeature)

			configuration {
				append(configuration)
			}
		}

		assertSame(expected = "bar", actual = raptor.configuration.value("foo").string())
	}


	@Test
	fun testZeroConfigurations() {
		val raptor = raptor {
			install(RaptorConfigurationFeature)
		}

		assertSame(expected = EmptyRaptorSettings, actual = raptor.configuration)
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
