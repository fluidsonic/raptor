package tests

import io.fluidsonic.country.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// `Country` and `CountryCode` both encode as a plain BSON string. `Country` decodes via
// `CountryCode`, so these two types share one golden wire format ("DE") and one test file.
private data class CountryHolder(val value: Country?)


private fun countryHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<CountryHolder> {
		decode {
			reader.document {
				CountryHolder(value = value<Country?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


class CountryBsonTests {

	@Test
	fun encodesNonNullCountryAsStringCode() {
		val codec = codecFor(Country::class)
		val country = Country.forCode(CountryCode.parse("DE"))

		val actualBytes = codec.encodeFieldBytes("value", country)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("DE")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesNonNullCountryFromStringCode() {
		val codec = codecFor(Country::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("DE")
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = Country.forCode(CountryCode.parse("DE")))
	}


	@Test
	fun encodesNonNullCountryCodeAsString() {
		val codec = codecFor(CountryCode::class)
		val code = CountryCode.parse("US")

		val actualBytes = codec.encodeFieldBytes("value", code)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("US")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = code)
	}


	@Test
	fun rejectsWrongBsonTypeForCountry() {
		val codec = codecFor(Country::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt32(42)
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	// By default, writing a null value through `RaptorBsonWriter.value(field, value, preserveNull = false)`
	// omits the field entirely rather than writing an explicit BSON null — the field goes missing, not null.
	@Test
	fun omittingNullByDefaultProducesAMissingFieldNotBsonNull() {
		val registry = testRegistryWith(countryHolderDefinition(preserveNull = false))
		val codec = registry.get(CountryHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(CountryHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(countryHolderDefinition(preserveNull = true))
		val codec = registry.get(CountryHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(CountryHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableCountry() {
		val registry = testRegistryWith(countryHolderDefinition(preserveNull = true))
		val codec = registry.get(CountryHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = CountryHolder(value = null))
	}


	@Test
	fun decodesNonNullCountryThroughNullableReifiedPath() {
		val registry = testRegistryWith(countryHolderDefinition(preserveNull = true))
		val codec = registry.get(CountryHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeString("FR")
		}

		assertEquals(
			actual = codec.decodeTopLevelValue(bytes),
			expected = CountryHolder(value = Country.forCode(CountryCode.parse("FR"))),
		)
	}


	@Test
	fun encodesAndDecodesCountryAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val countries = listOf(Country.forCode(CountryCode.parse("DE")), Country.forCode(CountryCode.parse("US")))

		val actualBytes = listCodec.encodeFieldBytes("value", countries)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("DE")
			writeString("US")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<Country>())),
		)

		assertEquals(actual = decoded, expected = countries)
	}
}
