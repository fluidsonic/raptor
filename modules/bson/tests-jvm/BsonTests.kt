package tests

import io.fluidsonic.country.*
import io.fluidsonic.currency.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.test.*


class BsonTests {

	@Test
	fun testBson() {
		val codec = DummyBsonCodec
		val definition = Country.bsonDefinition()
		val provider = DummyBsonCodecProvider

		val raptor = raptor {
			install(RaptorBsonPlugin)

			bson {
				codecs(codec)
				definitions(definition)
				providers(provider)
			}
		}

		val configuration = raptor.context.bson

		assertEquals(
			expected = listOf(
				RaptorBsonDefinition.of(codec),
				definition,
				RaptorBsonDefinition.of(provider),
			),
			actual = configuration.definitions.toList()
		)
	}


	@Test
	fun testDefaultCodecs() {
		val countryDefinition = Country.bsonDefinition()
		val currencyDefinition = Currency.bsonDefinition()

		val raptor = raptor {
			install(RaptorBsonPlugin)

			bson {
				definitions(countryDefinition)
				includeDefaultDefinitions()
				definitions(currencyDefinition)
			}
		}

		val configuration = raptor.context.bson

		assertEquals(
			expected = listOf(countryDefinition) + currencyDefinition + RaptorBsonDefinition.defaults,
			actual = configuration.definitions
		)
	}


	@Test
	fun testEmpty() {
		val raptor = raptor {
			install(RaptorBsonPlugin)
		}

		val configuration = raptor.context.bson

		assertEquals(expected = emptyList(), actual = configuration.definitions)
	}


	@Test
	fun testWithoutInstallationFails() {
		val raptor = raptor {}

		assertEquals(
			expected = "Plugin io.fluidsonic.raptor.bson.RaptorBsonPlugin is not installed.",
			actual = assertFails {
				raptor.context.bson
			}.message
		)
	}
}
