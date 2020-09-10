package tests

import io.fluidsonic.country.*
import io.fluidsonic.currency.*
import io.fluidsonic.raptor.*
import kotlin.test.*
import org.bson.codecs.configuration.*


class BsonTests {

	@Test
	fun testBson() {
		val codec = DummyBsonCodec
		val definition = Country.bsonDefinition()
		val provider = DummyBsonCodecProvider
		val registry = CodecRegistries.fromCodecs(codec)

		val raptor = raptor {
			install(BsonRaptorFeature) {
				codecs(codec)
				definitions(definition)
			}

			bson {
				providers(provider)
				registries(registry)
			}
		}

		val configuration = raptor.context.bsonConfiguration

		assertEquals(
			expected = listOf(
				RaptorBsonDefinition.of(codec),
				definition,
				RaptorBsonDefinition.of(provider),
				RaptorBsonDefinition.of(registry),
			),
			actual = configuration.definitions.underlyingDefinitions.toList()
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
			actual = configuration.definitions.underlyingDefinitions
		)
	}


	@Test
	fun testEmpty() {
		val raptor = raptor {
			install(BsonRaptorFeature)
		}

		val configuration = raptor.context.bsonConfiguration

		assertEquals(expected = RaptorBsonDefinition.empty, actual = configuration.definitions)
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
