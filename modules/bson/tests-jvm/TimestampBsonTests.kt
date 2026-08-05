package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.time.*
import kotlin.reflect.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// `Timestamp` (a typealias for `kotlin.time.Instant`) maps one-to-one onto a BSON DateTime of
// `value.toEpochMilliseconds()`. The epoch milliseconds below are chosen directly rather than derived
// from another library call, so the assertions are not circular.
private const val epochMillisecondsOf2024_01_15T10_30_00Z = 1_705_314_600_000L
private const val epochMillisecondsOf1969_12_31T23_59_59Z = -1_000L


private data class TimestampHolder(val value: Timestamp?)


private fun timestampHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<TimestampHolder> {
		decode {
			reader.document {
				TimestampHolder(value = value<Timestamp?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


class TimestampBsonTests {

	@Test
	fun encodesTimestampAsBsonDateTimeOfEpochMilliseconds() {
		val codec = codecFor(Timestamp::class)

		val actualBytes = codec.encodeFieldBytes("value", Timestamp.fromEpochMilliseconds(epochMillisecondsOf2024_01_15T10_30_00Z))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(epochMillisecondsOf2024_01_15T10_30_00Z)
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesPreEpochTimestampAsNegativeBsonDateTime() {
		val codec = codecFor(Timestamp::class)

		val actualBytes = codec.encodeFieldBytes("value", Timestamp.fromEpochMilliseconds(epochMillisecondsOf1969_12_31T23_59_59Z))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDateTime(epochMillisecondsOf1969_12_31T23_59_59Z)
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesTimestampFromBsonDateTime() {
		val codec = codecFor(Timestamp::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(epochMillisecondsOf2024_01_15T10_30_00Z)
		}

		assertEquals(
			actual = codec.decodeFieldValue(bytes, "value"),
			expected = Timestamp.fromEpochMilliseconds(epochMillisecondsOf2024_01_15T10_30_00Z),
		)
	}


	@Test
	fun decodesPreEpochTimestampFromNegativeBsonDateTime() {
		val codec = codecFor(Timestamp::class)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(epochMillisecondsOf1969_12_31T23_59_59Z)
		}

		assertEquals(
			actual = codec.decodeFieldValue(bytes, "value"),
			expected = Timestamp.fromEpochMilliseconds(epochMillisecondsOf1969_12_31T23_59_59Z),
		)
	}


	// A BSON Int64 carries the same 64-bit payload as a BSON DateTime but is a distinct BSON type,
	// so `readDateTime()` must reject it rather than silently reinterpreting the bits.
	@Test
	fun rejectsBsonInt64ForTimestamp() {
		val codec = codecFor(Timestamp::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt64(epochMillisecondsOf2024_01_15T10_30_00Z)
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun rejectsBsonStringForTimestamp() {
		val codec = codecFor(Timestamp::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("2024-01-15T10:30:00Z")
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun omittingNullByDefaultProducesAMissingFieldNotBsonNull() {
		val registry = testRegistryWith(timestampHolderDefinition(preserveNull = false))
		val codec = registry.get(TimestampHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(TimestampHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(timestampHolderDefinition(preserveNull = true))
		val codec = registry.get(TimestampHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(TimestampHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableTimestamp() {
		val registry = testRegistryWith(timestampHolderDefinition(preserveNull = true))
		val codec = registry.get(TimestampHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = TimestampHolder(value = null))
	}


	@Test
	fun decodesNonNullTimestampThroughNullableReifiedPath() {
		val registry = testRegistryWith(timestampHolderDefinition(preserveNull = true))
		val codec = registry.get(TimestampHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeDateTime(epochMillisecondsOf2024_01_15T10_30_00Z)
		}

		assertEquals(
			actual = codec.decodeTopLevelValue(bytes),
			expected = TimestampHolder(value = Timestamp.fromEpochMilliseconds(epochMillisecondsOf2024_01_15T10_30_00Z)),
		)
	}


	@Test
	fun encodesAndDecodesTimestampAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val timestamps = listOf(
			Timestamp.fromEpochMilliseconds(epochMillisecondsOf2024_01_15T10_30_00Z),
			Timestamp.fromEpochMilliseconds(epochMillisecondsOf1969_12_31T23_59_59Z),
		)

		val actualBytes = listCodec.encodeFieldBytes("value", timestamps)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeDateTime(epochMillisecondsOf2024_01_15T10_30_00Z)
			writeDateTime(epochMillisecondsOf1969_12_31T23_59_59Z)
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<Timestamp>())),
		)

		assertEquals(actual = decoded, expected = timestamps)
	}
}
