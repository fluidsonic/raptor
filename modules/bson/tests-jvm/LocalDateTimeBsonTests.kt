package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import kotlinx.datetime.LocalDateTime
import org.bson.*
import tests.utility.*


// `LocalDateTime` encodes as a BSON DateTime holding the epoch milliseconds of that date-time
// interpreted as UTC. All expected epoch milliseconds below are computed with plain `java.time` so the
// assertions do not depend on the very kotlinx-datetime conversions under test.
private data class LocalDateTimeHolder(val value: LocalDateTime?)


private fun localDateTimeHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<LocalDateTimeHolder> {
		decode {
			reader.document {
				LocalDateTimeHolder(value = value<LocalDateTime?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


private fun utcEpochMilliseconds(
	year: Int,
	month: Int,
	day: Int,
	hour: Int,
	minute: Int,
	second: Int,
	nanosecond: Int = 0,
): Long =
	java.time.LocalDateTime.of(year, month, day, hour, minute, second, nanosecond)
		.toInstant(java.time.ZoneOffset.UTC)
		.toEpochMilli()


class LocalDateTimeBsonTests {

	@Test
	fun encodesLocalDateTimeAsBsonDateTimeInterpretedAsUtc() {
		val codec = codecFor(LocalDateTime::class)

		val actualBytes = codec.encodeFieldBytes("value", LocalDateTime(2024, 1, 15, 10, 30, 0))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(utcEpochMilliseconds(2024, 1, 15, 10, 30, 0))
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesPreEpochLocalDateTimeWithMillisecondsAsNegativeBsonDateTime() {
		val codec = codecFor(LocalDateTime::class)

		val actualBytes = codec.encodeFieldBytes("value", LocalDateTime(1969, 7, 20, 20, 17, 40, 999_000_000))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(utcEpochMilliseconds(1969, 7, 20, 20, 17, 40, nanosecond = 999_000_000))
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesLocalDateTimeFromBsonDateTime() {
		val codec = codecFor(LocalDateTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(utcEpochMilliseconds(2024, 1, 15, 10, 30, 0))
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = LocalDateTime(2024, 1, 15, 10, 30, 0))
	}


	@Test
	fun decodesPreEpochLocalDateTimeWithMillisecondsFromNegativeBsonDateTime() {
		val codec = codecFor(LocalDateTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(utcEpochMilliseconds(1969, 7, 20, 20, 17, 40, nanosecond = 999_000_000))
		}

		assertEquals(
			actual = codec.decodeFieldValue(bytes, "value"),
			expected = LocalDateTime(1969, 7, 20, 20, 17, 40, 999_000_000),
		)
	}


	// A BSON Int64 carries the same 64-bit payload as a BSON DateTime but is a distinct BSON type,
	// so `readDateTime()` must reject it rather than silently reinterpreting the bits.
	@Test
	fun rejectsBsonInt64ForLocalDateTime() {
		val codec = codecFor(LocalDateTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt64(utcEpochMilliseconds(2024, 1, 15, 10, 30, 0))
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun rejectsBsonStringForLocalDateTime() {
		val codec = codecFor(LocalDateTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("2024-01-15T10:30:00")
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun omittingNullByDefaultProducesAMissingFieldNotBsonNull() {
		val registry = testRegistryWith(localDateTimeHolderDefinition(preserveNull = false))
		val codec = registry.get(LocalDateTimeHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(LocalDateTimeHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(localDateTimeHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalDateTimeHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(LocalDateTimeHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableLocalDateTime() {
		val registry = testRegistryWith(localDateTimeHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalDateTimeHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = LocalDateTimeHolder(value = null))
	}


	@Test
	fun decodesNonNullLocalDateTimeThroughNullableReifiedPath() {
		val registry = testRegistryWith(localDateTimeHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalDateTimeHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(utcEpochMilliseconds(2024, 1, 15, 10, 30, 0))
		}

		assertEquals(
			actual = codec.decodeTopLevelValue(bytes),
			expected = LocalDateTimeHolder(value = LocalDateTime(2024, 1, 15, 10, 30, 0)),
		)
	}


	@Test
	fun encodesAndDecodesLocalDateTimeAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val dateTimes = listOf(LocalDateTime(2024, 1, 15, 10, 30, 0), LocalDateTime(1969, 7, 20, 20, 17, 40))

		val actualBytes = listCodec.encodeFieldBytes("value", dateTimes)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeDateTime(utcEpochMilliseconds(2024, 1, 15, 10, 30, 0))
			writeDateTime(utcEpochMilliseconds(1969, 7, 20, 20, 17, 40))
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<LocalDateTime>())),
		)

		assertEquals(actual = decoded, expected = dateTimes)
	}
}
