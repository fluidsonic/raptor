package tests

import io.fluidsonic.country.*
import io.fluidsonic.currency.*
import io.fluidsonic.raptor.*
import kotlin.test.*


class BsonTests {

	@Test
	fun testBson() {
		val codec = DummyBsonCodec
		val definition = Country.bsonDefinition()
		val provider = DummyBsonCodecProvider

		val raptor = raptor {
			install(BsonRaptorFeature) {
				codecs(codec)
				definitions(definition)
			}

			bson {
				providers(provider)
			}
		}

		val configuration = raptor.context.bsonConfiguration

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
			install(BsonRaptorFeature) {
				definitions(countryDefinition)
				includeDefaultDefinitions()
				definitions(currencyDefinition)
			}
		}

		val configuration = raptor.context.bsonConfiguration

		assertEquals(
			expected = listOf(countryDefinition) + currencyDefinition + RaptorBsonDefaults.definitions,
			actual = configuration.definitions
		)
	}


	@Test
	fun testEmpty() {
		val raptor = raptor {
			install(BsonRaptorFeature)
		}

		val configuration = raptor.context.bsonConfiguration

		assertEquals(expected = emptyList(), actual = configuration.definitions)
	}


	@Test
	fun testWithoutInstallationFails() {
		val raptor = raptor {
			bson.definitions(Country.bsonDefinition())
		}

		assertEquals(
			expected = "You must install BsonRaptorFeature for enabling BSON functionality.",
			actual = assertFails {
				raptor.context.bsonConfiguration
			}.message
		)
	}
}
