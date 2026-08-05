package tests

import io.fluidsonic.currency.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// `Currency` and `CurrencyCode` both encode as a plain BSON string. `Currency` decodes via
// `CurrencyCode`, so these two types share one golden wire format ("USD") and one test file.
private data class CurrencyHolder(val value: Currency?)


private fun currencyHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<CurrencyHolder> {
		decode {
			reader.document {
				CurrencyHolder(value = value<Currency?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


class CurrencyBsonTests {

	@Test
	fun encodesNonNullCurrencyAsStringCode() {
		val codec = codecFor(Currency::class)
		val currency = Currency.forCode(CurrencyCode.parse("USD"))

		val actualBytes = codec.encodeFieldBytes("value", currency)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("USD")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesNonNullCurrencyFromStringCode() {
		val codec = codecFor(Currency::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("USD")
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = Currency.forCode(CurrencyCode.parse("USD")))
	}


	@Test
	fun encodesNonNullCurrencyCodeAsString() {
		val codec = codecFor(CurrencyCode::class)
		val code = CurrencyCode.parse("EUR")

		val actualBytes = codec.encodeFieldBytes("value", code)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("EUR")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = code)
	}


	@Test
	fun rejectsWrongBsonTypeForCurrency() {
		val codec = codecFor(Currency::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt32(42)
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun rejectsWrongBsonTypeForCurrencyCode() {
		val codec = codecFor(CurrencyCode::class)

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
		val registry = testRegistryWith(currencyHolderDefinition(preserveNull = false))
		val codec = registry.get(CurrencyHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(CurrencyHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(currencyHolderDefinition(preserveNull = true))
		val codec = registry.get(CurrencyHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(CurrencyHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableCurrency() {
		val registry = testRegistryWith(currencyHolderDefinition(preserveNull = true))
		val codec = registry.get(CurrencyHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = CurrencyHolder(value = null))
	}


	@Test
	fun decodesNonNullCurrencyThroughNullableReifiedPath() {
		val registry = testRegistryWith(currencyHolderDefinition(preserveNull = true))
		val codec = registry.get(CurrencyHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeString("JPY")
		}

		assertEquals(
			actual = codec.decodeTopLevelValue(bytes),
			expected = CurrencyHolder(value = Currency.forCode(CurrencyCode.parse("JPY"))),
		)
	}


	@Test
	fun encodesAndDecodesCurrencyAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val currencies = listOf(Currency.forCode(CurrencyCode.parse("USD")), Currency.forCode(CurrencyCode.parse("EUR")))

		val actualBytes = listCodec.encodeFieldBytes("value", currencies)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("USD")
			writeString("EUR")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<Currency>())),
		)

		assertEquals(actual = decoded, expected = currencies)
	}
}
