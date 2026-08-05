package tests

import java.time.*
import kotlin.test.*
import tests.utility.*


// `DayOfWeek` encodes as a plain BSON string holding the lowercase English weekday name.
// The definition maps each of the seven enum constants explicitly and rejects anything else,
// so there is no factory-function decode path to exercise beyond the string mapping itself.
class DayOfWeekBsonTests {

	@Test
	fun encodesMondayAsLowercaseName() {
		val codec = codecFor(DayOfWeek::class)

		val actualBytes = codec.encodeFieldBytes("value", DayOfWeek.MONDAY)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("monday")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesMondayFromLowercaseName() {
		val codec = codecFor(DayOfWeek::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("monday")
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = DayOfWeek.MONDAY)
	}


	@Test
	fun encodesAndDecodesSundayAsLowercaseName() {
		val codec = codecFor(DayOfWeek::class)

		val actualBytes = codec.encodeFieldBytes("value", DayOfWeek.SUNDAY)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("sunday")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = DayOfWeek.SUNDAY)
	}


	@Test
	fun encodesAndDecodesWednesdayAsLowercaseName() {
		val codec = codecFor(DayOfWeek::class)

		val actualBytes = codec.encodeFieldBytes("value", DayOfWeek.WEDNESDAY)
		val goldenBytes = documentBytes {
			writeName("value")
			writeString("wednesday")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(actualBytes, "value"), expected = DayOfWeek.WEDNESDAY)
	}


	// The uppercase enum name is not the wire format — only the lowercase spelling decodes.
	@Test
	fun rejectsUppercaseEnumName() {
		val codec = codecFor(DayOfWeek::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("MONDAY")
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun rejectsUnrecognizedDayOfWeekString() {
		val codec = codecFor(DayOfWeek::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("notaday")
		}

		assertEquals(
			actual = assertFails { codec.decodeFieldValue(bytes, "value") }.message,
			expected = "invalid day of week: notaday",
		)
	}


	@Test
	fun rejectsWrongBsonTypeForDayOfWeek() {
		val codec = codecFor(DayOfWeek::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt32(42)
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}
}
