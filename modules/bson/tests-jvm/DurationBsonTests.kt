package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.bson.*
import tests.utility.*


// `kotlin.time.Duration` is an inline value class over a `Long` and encodes as a plain BSON string
// holding an ISO-8601 duration (`PnDTnHnMnS`). Every golden string below is written out from the
// ISO-8601 grammar itself — the zero-component parts are omitted, and a zero duration degenerates to
// the seconds component `PT0S` — and matches what the unrelated `java.time.Duration.toString()` emits
// for the same amount. None of them is taken from `Duration.toIsoString()`, which is the code under test.
private data class DurationHolder(val value: Duration?)


private fun durationHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<DurationHolder> {
		decode {
			reader.document {
				DurationHolder(value = value<Duration?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


class DurationBsonTests {

	@Test
	fun encodesOneHourAsIsoString() {
		val codec = codecFor(Duration::class)

		val actualBytes = codec.encodeFieldBytes("value", 1.hours)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("PT1H")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesOneHourFromIsoString() {
		val codec = codecFor(Duration::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("PT1H")
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = 1.hours)
	}


	@Test
	fun encodesAndDecodesSubHourDurationAsIsoString() {
		val codec = codecFor(Duration::class)

		val actualBytes = codec.encodeFieldBytes("value", 30.minutes)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("PT30M")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = 30.minutes)
	}


	@Test
	fun encodesAndDecodesMultiComponentDurationAsIsoString() {
		val codec = codecFor(Duration::class)

		val actualBytes = codec.encodeFieldBytes("value", 90.minutes)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("PT1H30M")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = 90.minutes)
	}


	@Test
	fun encodesAndDecodesZeroDurationAsIsoString() {
		val codec = codecFor(Duration::class)

		val actualBytes = codec.encodeFieldBytes("value", Duration.ZERO)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("PT0S")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = Duration.ZERO)
	}


	@Test
	fun rejectsUnparsableIsoString() {
		val codec = codecFor(Duration::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("notaduration")
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun rejectsWrongBsonTypeForDuration() {
		val codec = codecFor(Duration::class)

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
		val registry = testRegistryWith(durationHolderDefinition(preserveNull = false))
		val codec = registry.get(DurationHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(DurationHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(durationHolderDefinition(preserveNull = true))
		val codec = registry.get(DurationHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(DurationHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableDuration() {
		val registry = testRegistryWith(durationHolderDefinition(preserveNull = true))
		val codec = registry.get(DurationHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = DurationHolder(value = null))
	}


	@Test
	fun decodesNonNullDurationThroughNullableReifiedPath() {
		val registry = testRegistryWith(durationHolderDefinition(preserveNull = true))
		val codec = registry.get(DurationHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeString("PT30M")
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = DurationHolder(value = 30.minutes))
	}


	@Test
	fun encodesAndDecodesDurationAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val durations = listOf(1.hours, Duration.ZERO)

		val actualBytes = listCodec.encodeFieldBytes("value", durations)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("PT1H")
			writeString("PT0S")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<Duration>())),
		)

		assertEquals(actual = decoded, expected = durations)
	}
}
