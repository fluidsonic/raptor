package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.reflect.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// `Set` has no entry in `RaptorBsonDefinition.raptorDefaults` at all — it is special-cased inside
// `DefaultBsonReaderScope.value(type)` and therefore only reachable through the reified
// `reader.value<Set<T>>()` path, never through the codec registry. Hence the holder definition.
private data class SetHolder(val value: Set<String>)


private fun setHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<SetHolder> {
		decode {
			reader.document {
				SetHolder(value = value<Set<String>>("value"))
			}
		}

		// There is no `writer.value(field, Set<T>, …)` overload, so the array is written manually.
		encode { holder ->
			writer.document {
				array("value") {
					for (element in holder.value)
						value(element)
				}
			}
		}
	}


// `List<*>` and `Collection<*>` are registered as two independent, literally duplicated
// `RaptorBsonDefinition`s. Every wire-format test below therefore runs against both codecs
// separately — a future change must not let them diverge silently.
//
// Although both codecs write a *bare* array (no enclosing document) at their own position, they can
// only ever be exercised as a document field: `BsonBinaryWriter` rejects a root-level array outright
// ("A StartArray value cannot be written to the root level of a BSON document."), and
// `BsonBinaryReader` implies DOCUMENT for the top level. Hence `encodeFieldBytes`/`decodeFieldValue`
// with `documentBytes` goldens throughout — the goldens still pin the exact array bytes, and the
// absence of a nested `writeStartDocument` is what proves the array is bare.
class CollectionsBsonTests {

	@Test
	fun encodesAndDecodesTypedListOfStrings() {
		val codec = codecFor(List::class)
		val strings = listOf("a", "b")

		val actualBytes = codec.encodeFieldBytes("value", strings)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("a")
			writeString("b")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = codec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<String>())),
		)

		assertEquals(actual = decoded, expected = strings)
	}


	@Test
	fun encodesAndDecodesTypedCollectionOfStrings() {
		val codec = codecFor(Collection::class)
		val strings = listOf("a", "b")

		val actualBytes = codec.encodeFieldBytes("value", strings)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("a")
			writeString("b")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = codec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<String>())),
		)

		assertEquals(actual = decoded, expected = strings)
	}


	@Test
	fun encodesNonListCollectionOfStrings() {
		val codec = codecFor(Collection::class)
		val strings = linkedSetOf("a", "b")

		val actualBytes = codec.encodeFieldBytes("value", strings)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("a")
			writeString("b")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	// With `arguments = null` the decoder falls back to a `BsonTypeCodecMap` over the registry's
	// `bsonDefaults`, decoding every element according to its own raw BSON type.
	@Test
	fun decodesUntypedHeterogeneousArrayAsList() {
		val codec = codecFor(List::class)

		val bytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("a")
			writeInt32(1)
			writeBoolean(true)
			writeEndArray()
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = listOf("a", 1, true))
	}


	@Test
	fun decodesUntypedHeterogeneousArrayAsCollection() {
		val codec = codecFor(Collection::class)

		val bytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("a")
			writeInt32(1)
			writeBoolean(true)
			writeEndArray()
		}

		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = listOf("a", 1, true))
	}


	@Test
	fun encodesEmptyListAsEmptyArray() {
		val codec = codecFor(List::class)

		val actualBytes = codec.encodeFieldBytes("value", emptyList<String>())
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesEmptyCollectionAsEmptyArray() {
		val codec = codecFor(Collection::class)

		val actualBytes = codec.encodeFieldBytes("value", emptyList<String>())
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesEmptyArrayAsEmptyList() {
		val codec = codecFor(List::class)

		val bytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeEndArray()
		}

		val typedDecoded = codec.decodeFieldValue(
			bytes = bytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<String>())),
		)

		assertEquals(actual = typedDecoded, expected = emptyList<String>())
		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = emptyList<Any?>())
	}


	@Test
	fun decodesEmptyArrayAsEmptyCollection() {
		val codec = codecFor(Collection::class)

		val bytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeEndArray()
		}

		val typedDecoded = codec.decodeFieldValue(
			bytes = bytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<String>())),
		)

		assertEquals(actual = typedDecoded, expected = emptyList<String>())
		assertEquals(actual = codec.decodeFieldValue(bytes, "value"), expected = emptyList<Any?>())
	}


	@Test
	fun rejectsNonArrayBsonTypeForList() {
		val codec = codecFor(List::class)

		val bytes = documentBytes {
			writeName("value")
			writeString("not an array")
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}

		assertFails {
			codec.decodeFieldValue(
				bytes = bytes,
				fieldName = "value",
				arguments = listOf(KTypeProjection.covariant(typeOf<String>())),
			)
		}
	}


	@Test
	fun rejectsNonArrayBsonTypeForCollection() {
		val codec = codecFor(Collection::class)

		val bytes = documentBytes {
			writeName("value")
			writeInt32(42)
		}

		assertFails {
			codec.decodeFieldValue(bytes, "value")
		}

		assertFails {
			codec.decodeFieldValue(
				bytes = bytes,
				fieldName = "value",
				arguments = listOf(KTypeProjection.covariant(typeOf<String>())),
			)
		}
	}


	@Test
	fun encodesAndDecodesSetThroughReifiedReaderPath() {
		val registry = testRegistryWith(setHolderDefinition())
		val codec = registry.get(SetHolder::class.java)
		val holder = SetHolder(value = setOf("a", "b"))

		val actualBytes = codec.encodeTopLevelBytes(holder)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeString("a")
			writeString("b")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}
}
