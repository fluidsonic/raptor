package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.stdlib.*
import org.bson.codecs.configuration.*
import kotlin.test.*


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

		assertEquals(expected = listOf(codec), actual = configuration.codecs)
		assertEquals(expected = listOf(definition), actual = configuration.definitions)
		assertEquals(expected = listOf(provider), actual = configuration.providers)
		assertEquals(expected = listOf(registry), actual = configuration.registries)
	}


	@Test
	fun testDefaultCodecs() {
		val centsDefinition = Cents.bsonDefinition()
		val countryDefinition = Country.bsonDefinition()

		val raptor = raptor {
			install(BsonRaptorFeature) {
				definitions(countryDefinition)
				includeDefaultCodecs()
				definitions(centsDefinition)
			}
		}

		val configuration = raptor.context.bsonConfiguration

		assertEquals(expected = emptyList(), actual = configuration.codecs)
		assertEquals(
			expected = listOf(countryDefinition) + BsonDefaults.definitions + centsDefinition,
			actual = configuration.definitions
		)
		assertEquals(expected = BsonDefaults.providers, actual = configuration.providers)
		assertEquals(expected = emptyList(), actual = configuration.registries)
	}


	@Test
	fun testEmpty() {
		val raptor = raptor {
			install(BsonRaptorFeature)
		}

		val configuration = raptor.context.bsonConfiguration

		assertTrue(configuration.codecs.isEmpty())
		assertTrue(configuration.definitions.isEmpty())
		assertTrue(configuration.providers.isEmpty())
		assertTrue(configuration.registries.isEmpty())
	}


	@Test
	fun testWithoutInstallationFails() {
		val raptor = raptor {
			bson.definitions(Country.bsonDefinition())
		}

		assertEquals(
			expected = "You must install BsonRaptorFeature to access the BSON configuration.",
			actual = assertFails {
				raptor.context.bsonConfiguration
			}.message
		)
	}
}
