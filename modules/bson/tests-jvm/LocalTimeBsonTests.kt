package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import kotlinx.datetime.LocalTime
import org.bson.*
import tests.utility.*


// `LocalTime` encodes as a BSON DateTime holding the epoch milliseconds of that time of day on the Unix
// epoch date (1970-01-01) in UTC. All expected epoch milliseconds below are computed with plain
// `java.time` so the assertions do not depend on the very kotlinx-datetime conversions under test.
private data class LocalTimeHolder(val value: LocalTime?)


private fun localTimeHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<LocalTimeHolder> {
		decode {
			reader.document {
				LocalTimeHolder(value = value<LocalTime?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


private fun epochDateEpochMilliseconds(hour: Int, minute: Int, second: Int, nanosecond: Int = 0): Long =
	java.time.LocalTime.of(hour, minute, second, nanosecond)
		.atDate(java.time.LocalDate.of(1970, 1, 1))
		.toInstant(java.time.ZoneOffset.UTC)
		.toEpochMilli()


class LocalTimeBsonTests {

	@Test
	fun encodesLocalTimeAsBsonDateTimeOnTheEpochDate() {
		val codec = codecFor(LocalTime::class)

		val actualBytes = codec.encodeFieldBytes("value", LocalTime(10, 30, 0))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(epochDateEpochMilliseconds(10, 30, 0))
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesEndOfDayLocalTimeWithMillisecondsAsBsonDateTime() {
		val codec = codecFor(LocalTime::class)

		val actualBytes = codec.encodeFieldBytes("value", LocalTime(23, 59, 59, 999_000_000))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(epochDateEpochMilliseconds(23, 59, 59, nanosecond = 999_000_000))
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesMidnightLocalTimeAsBsonDateTimeZero() {
		val codec = codecFor(LocalTime::class)

		val actualBytes = codec.encodeFieldBytes("value", LocalTime(0, 0, 0))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(0L)
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = epochDateEpochMilliseconds(0, 0, 0), expected = 0L)
	}


	@Test
	fun decodesLocalTimeFromBsonDateTime() {
		val codec = codecFor(LocalTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(epochDateEpochMilliseconds(10, 30, 0))
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = LocalTime(10, 30, 0))
	}


	@Test
	fun decodesEndOfDayLocalTimeWithMillisecondsFromBsonDateTime() {
		val codec = codecFor(LocalTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(epochDateEpochMilliseconds(23, 59, 59, nanosecond = 999_000_000))
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = LocalTime(23, 59, 59, 999_000_000))
	}


	// Decoding only keeps the time of day: a BSON DateTime on any other date yields the same `LocalTime`.
	@Test
	fun decodesLocalTimeIgnoringTheDatePartOfTheBsonDateTime() {
		val codec = codecFor(LocalTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(
				java.time.LocalDateTime.of(2024, 1, 15, 10, 30, 0)
					.toInstant(java.time.ZoneOffset.UTC)
					.toEpochMilli(),
			)
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = LocalTime(10, 30, 0))
	}


	// A BSON Int64 carries the same 64-bit payload as a BSON DateTime but is a distinct BSON type,
	// so `readDateTime()` must reject it rather than silently reinterpreting the bits.
	@Test
	fun rejectsBsonInt64ForLocalTime() {
		val codec = codecFor(LocalTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt64(epochDateEpochMilliseconds(10, 30, 0))
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun rejectsBsonStringForLocalTime() {
		val codec = codecFor(LocalTime::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("10:30")
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun omittingNullByDefaultProducesAMissingFieldNotBsonNull() {
		val registry = testRegistryWith(localTimeHolderDefinition(preserveNull = false))
		val codec = registry.get(LocalTimeHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(LocalTimeHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(localTimeHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalTimeHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(LocalTimeHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableLocalTime() {
		val registry = testRegistryWith(localTimeHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalTimeHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = LocalTimeHolder(value = null))
	}


	@Test
	fun decodesNonNullLocalTimeThroughNullableReifiedPath() {
		val registry = testRegistryWith(localTimeHolderDefinition(preserveNull = true))
		val codec = registry.get(LocalTimeHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(epochDateEpochMilliseconds(10, 30, 0))
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = LocalTimeHolder(value = LocalTime(10, 30, 0)))
	}


	@Test
	fun encodesAndDecodesLocalTimeAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val times = listOf(LocalTime(10, 30, 0), LocalTime(23, 59, 59, 999_000_000))

		val actualBytes = listCodec.encodeFieldBytes("value", times)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeDateTime(epochDateEpochMilliseconds(10, 30, 0))
			writeDateTime(epochDateEpochMilliseconds(23, 59, 59, nanosecond = 999_000_000))
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<LocalTime>())),
		)

		assertEquals(actual = decoded, expected = times)
	}
}
