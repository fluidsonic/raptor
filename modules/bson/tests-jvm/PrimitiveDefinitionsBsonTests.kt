package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// `RaptorBsonDefinitionBuilder` carries concrete, unboxed `decode`/`encode` overloads for the primitive wire
// formats. They are selected by passing a callable reference, exactly as `extensions/Country.kt` does — not
// by a lambda, whose trailing expression Kotlin happily coerces to `Unit` and thereby selects the primary
// `encode { }` overload instead, and not by `decode<Double>`, whose explicit type argument forces the reified
// generic overload.
private data class Ratio(val value: Double)


private fun ratioDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<Ratio> {
		decode(::Ratio)
		encode(Ratio::value)
	}


private data class Flag(val value: Boolean)


private fun flagDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<Flag> {
		decode(::Flag)
		encode(Flag::value)
	}


class PrimitiveDefinitionsBsonTests {

	@Test
	fun encodesAndDecodesDoubleBackedValueType() {
		val registry = testRegistryWith(ratioDefinition())
		val codec = registry.get(Ratio::class.java)

		val actualBytes = codec.encodeFieldBytes("value", Ratio(value = 0.25))
		val goldenBytes = documentBytes {
			writeName("value")
			writeDouble(0.25)
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(goldenBytes, "value"), expected = Ratio(value = 0.25))
	}


	// The concrete `Double` overload reads `reader.double()` rather than resolving org.bson's `DoubleCodec`,
	// so a stored INT32 is rejected instead of being widened. This is what proves the concrete overload — not
	// the reified generic one — is the overload the compiler selects here.
	@Test
	fun doubleBackedValueTypeRejectsWidenedInt32() {
		val registry = testRegistryWith(ratioDefinition())
		val codec = registry.get(Ratio::class.java)

		val bytes = documentBytes {
			writeName("value")
			writeInt32(1)
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}
	}


	@Test
	fun encodesAndDecodesBooleanBackedValueType() {
		val registry = testRegistryWith(flagDefinition())
		val codec = registry.get(Flag::class.java)

		val actualBytes = codec.encodeFieldBytes("value", Flag(value = true))
		val goldenBytes = documentBytes {
			writeName("value")
			writeBoolean(true)
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeFieldValue(goldenBytes, "value"), expected = Flag(value = true))
		assertEquals(actual = codec.decodeFieldValue(documentBytes {
			writeName("value")
			writeBoolean(false)
		}, "value"), expected = Flag(value = false))
	}
}
