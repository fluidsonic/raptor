package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import kotlinx.datetime.LocalDate
import org.bson.*
import tests.utility.*


// `LocalDate` encodes as a BSON DateTime holding the epoch milliseconds of that date's midnight in UTC.
// All expected epoch milliseconds below are computed with plain `java.time` so the assertions do not
// depend on the very kotlinx-datetime conversions under test.
private data class LocalDateHolder(val value: LocalDate?)


private fun localDateHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<LocalDateHolder> {
		decode {
			reader.document {
				LocalDateHolder(value = value<LocalDate?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


private fun startOfDayEpochMilliseconds(year: Int, month: Int, day: Int): Long =
	java.time.LocalDate.of(year, month, day)
		.atStartOfDay(java.time.ZoneOffset.UTC)
		.toInstant()
		.toEpochMilli()


class LocalDateBsonTests {

	@Test
	fun encodesLocalDateAsBsonDateTimeOfMidnightUtc() {
		val codec = codecFor(LocalDate::class)

		val actualBytes = codec.encodeFieldBytes("value", LocalDate(2024, 1, 15))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(startOfDayEpochMilliseconds(2024, 1, 15))
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesPreEpochLocalDateAsNegativeBsonDateTime() {
		val codec = codecFor(LocalDate::class)

		val actualBytes = codec.encodeFieldBytes("value", LocalDate(1969, 7, 20))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(startOfDayEpochMilliseconds(1969, 7, 20))
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesLocalDateFromBsonDateTime() {
		val codec = codecFor(LocalDate::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(startOfDayEpochMilliseconds(2024, 1, 15))
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = LocalDate(2024, 1, 15))
	}


	@Test
	fun decodesPreEpochLocalDateFromNegativeBsonDateTime() {
		val codec = codecFor(LocalDate::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(startOfDayEpochMilliseconds(1969, 7, 20))
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = LocalDate(1969, 7, 20))
	}


	// A BSON Int64 carries the same 64-bit payload as a BSON DateTime but is a distinct BSON type,
	// so `readDateTime()` must reject it rather than silently reinterpreting the bits.
	@Test
	fun rejectsBsonInt64ForLocalDate() {
		val codec = codecFor(LocalDate::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt64(startOfDayEpochMilliseconds(2024, 1, 15))
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun rejectsBsonStringForLocalDate() {
		val codec = codecFor(LocalDate::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("2024-01-15")
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun omittingNullByDefaultProducesAMissingFieldNotBsonNull() {
		val registry = testRegistryWith(localDateHolderDefinition(preserveNull = false))
		val codec = registry.get(LocalDateHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(LocalDateHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(localDateHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalDateHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(LocalDateHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableLocalDate() {
		val registry = testRegistryWith(localDateHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalDateHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = LocalDateHolder(value = null))
	}


	@Test
	fun decodesNonNullLocalDateThroughNullableReifiedPath() {
		val registry = testRegistryWith(localDateHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalDateHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(startOfDayEpochMilliseconds(2024, 1, 15))
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = LocalDateHolder(value = LocalDate(2024, 1, 15)))
	}


	@Test
	fun encodesAndDecodesLocalDateAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val dates = listOf(LocalDate(2024, 1, 15), LocalDate(1969, 7, 20))

		val actualBytes = listCodec.encodeFieldBytes("value", dates)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeDateTime(startOfDayEpochMilliseconds(2024, 1, 15))
			writeDateTime(startOfDayEpochMilliseconds(1969, 7, 20))
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<LocalDate>())),
		)

		assertEquals(actual = decoded, expected = dates)
	}
}
