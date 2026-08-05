package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import kotlinx.datetime.*
import org.bson.*
import tests.utility.*


// `TimeZone` encodes as a plain BSON string holding its IANA zone ID. Its definition encodes
// `includingSubclasses = true`, which is what makes the concrete runtime types behind `TimeZone.UTC`
// and `TimeZone.of(…)` encodable when they reach the writer as a plain `Any` (holder field, list element).
private data class TimeZoneHolder(val value: TimeZone?)


private fun timeZoneHolderDefinition(preserveNull: Boolean): RaptorBsonDefinition =
	raptor.bson.definition<TimeZoneHolder> {
		decode {
			reader.document {
				TimeZoneHolder(value = value<TimeZone?>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value, preserveNull = preserveNull)
			}
		}
	}


class TimeZoneBsonTests {

	@Test
	fun encodesUtcAsZoneId() {
		val codec = codecFor(TimeZone::class)

		val actualBytes = codec.encodeFieldBytes("value", TimeZone.UTC)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("UTC")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesUtcFromZoneId() {
		val codec = codecFor(TimeZone::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("UTC")
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = TimeZone.UTC)
	}


	@Test
	fun encodesAndDecodesRegionZoneAsZoneId() {
		val codec = codecFor(TimeZone::class)
		val timeZone = TimeZone.of("Europe/Berlin")

		val actualBytes = codec.encodeFieldBytes("value", timeZone)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("Europe/Berlin")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = timeZone)
	}


	@Test
	fun encodesAndDecodesSecondRegionZoneAsZoneId() {
		val codec = codecFor(TimeZone::class)
		val timeZone = TimeZone.of("America/New_York")

		val actualBytes = codec.encodeFieldBytes("value", timeZone)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("America/New_York")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = timeZone)
	}


	@Test
	fun rejectsInvalidTimeZoneId() {
		val codec = codecFor(TimeZone::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("Not/AZone")
		}

		assertEquals(
			actual = assertFails { codec.decodeFieldValue(bytes, "value") }.message,
			expected = "Invalid TimeZone ID: Not/AZone",
		)
	}


	@Test
	fun rejectsWrongBsonTypeForTimeZone() {
		val codec = codecFor(TimeZone::class)

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
		val registry = testRegistryWith(timeZoneHolderDefinition(preserveNull = false))
		val codec = registry.get(TimeZoneHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(TimeZoneHolder(value = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun preserveNullWritesAnExplicitBsonNull() {
		val registry = testRegistryWith(timeZoneHolderDefinition(preserveNull = true))
		val codec = registry.get(TimeZoneHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(TimeZoneHolder(value = null))
		val goldenBytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesExplicitBsonNullAsKotlinNullForNullableTimeZone() {
		val registry = testRegistryWith(timeZoneHolderDefinition(preserveNull = true))
		val codec = registry.get(TimeZoneHolder::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeNull()
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = TimeZoneHolder(value = null))
	}


	@Test
	fun encodesAndDecodesNonNullTimeZoneThroughNullableReifiedPath() {
		val registry = testRegistryWith(timeZoneHolderDefinition(preserveNull = true))
		val codec = registry.get(TimeZoneHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(TimeZoneHolder(value = TimeZone.of("Europe/Berlin")))
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("Europe/Berlin")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(
			actual = codec.decodeTopLevelValue(actualBytes),
			expected = TimeZoneHolder(value = TimeZone.of("Europe/Berlin")),
		)
	}


	@Test
	fun encodesAndDecodesTimeZoneAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val timeZones = listOf(TimeZone.UTC, TimeZone.of("Europe/Berlin"))

		val actualBytes = listCodec.encodeFieldBytes("value", timeZones)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("UTC")
			writeString("Europe/Berlin")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<TimeZone>())),
		)

		assertEquals(actual = decoded, expected = timeZones)
	}
}
