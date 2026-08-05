package tests.utility

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import org.bson.*
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.bson.io.BasicOutputBuffer


// Shared codec registry with every `raptorDefaults` + `bsonDefaults` definition installed, exactly
// as genki-core installs them. All round-trip tests resolve codecs through this single instance.
internal val testCodecRegistry: CodecRegistry by lazy {
	raptor {
		install(RaptorBsonPlugin)

		bson {
			includeDefaultDefinitions()
		}
	}.context.bson.codecRegistry
}


internal fun <Value : Any> codecFor(valueClass: KClass<Value>): Codec<Value> =
	testCodecRegistry.get(valueClass.java)


// A fresh registry carrying one or more test-local definitions alongside the defaults. Use this to test
// paths that only exist inside a `decode { }`/`encode { }` block — e.g. `reader.value<Country?>()` null
// handling, or `reader.value<Set<Country>>()` — which the external `Codec<T>` boundary alone can't reach,
// since there is no supported way to construct a `RaptorBsonReaderScope`/`RaptorBsonWriterScope` directly.
// Give each test-local host type its own concrete (non-generic) class: `RaptorBsonDefinition` is keyed by
// the erased `KClass`, so multiple differently-parameterized instances of one generic holder class would
// collide in the same registry.
internal fun testRegistryWith(vararg additionalDefinitions: RaptorBsonDefinition): CodecRegistry =
	raptor {
		install(RaptorBsonPlugin)

		bson {
			definitions(*additionalDefinitions)
			includeDefaultDefinitions()
		}
	}.context.bson.codecRegistry


// Writes a single top-level BSON document via raw `BsonWriter` calls and returns its wire bytes.
// Used both to build golden documents (hand-written expected bytes) and, via the `Codec`
// extensions below, to capture what Raptor's codecs actually produce.
internal fun documentBytes(write: BsonWriter.() -> Unit): ByteArray {
	val buffer = BasicOutputBuffer()

	BsonBinaryWriter(buffer).use { writer ->
		writer.writeStartDocument()
		writer.write()
		writer.writeEndDocument()
	}

	return buffer.toByteArray()
}


internal fun <Result> readDocument(bytes: ByteArray, read: BsonReader.() -> Result): Result =
	BsonBinaryReader(ByteBuffer.wrap(bytes)).use { reader ->
		reader.readStartDocument()

		val result = reader.read()

		reader.readEndDocument()

		result
	}


// For codecs whose value IS the whole top-level BSON value — a document-shaped type like `GeoCoordinate`
// (writes `document { }` itself), or a test-local host definition used to reach the reified
// `reader.value<T?>()` / `reader.value<Set<T>>()` paths. Unlike [encodeFieldBytes], this does not add a
// synthetic wrapping field — the codec owns the entire structure.
//
// This does NOT work for `List`/`Collection`, even though their codecs write a bare `array { }` at the
// position they're given: BSON requires the root value to always be a document — `AbstractBsonWriter`
// throws "A StartArray value cannot be written to the root level of a BSON document" if you try. Use
// [encodeFieldBytes]/[decodeFieldValue] for List/Collection instead, nesting the array inside a field.
internal fun <Value : Any> Codec<Value>.encodeTopLevelBytes(value: Value): ByteArray {
	val buffer = BasicOutputBuffer()

	BsonBinaryWriter(buffer).use { writer ->
		encode(writer, value, EncoderContext.builder().build())
	}

	return buffer.toByteArray()
}


internal fun <Value : Any> Codec<Value>.decodeTopLevelValue(bytes: ByteArray, arguments: List<KTypeProjection>? = null): Value =
	BsonBinaryReader(ByteBuffer.wrap(bytes)).use { reader ->
		when (val codec = this@decodeTopLevelValue) {
			is CodecEx<Value> -> codec.decode(reader, DecoderContext.builder().build(), arguments)
			else -> codec.decode(reader, DecoderContext.builder().build())
		}
	}


// Encodes `{ fieldName: value }` through the real registry codec for [Value] and returns the wire bytes.
internal fun <Value : Any> Codec<Value>.encodeFieldBytes(fieldName: String, value: Value): ByteArray =
	documentBytes {
		writeName(fieldName)
		encode(this, value, EncoderContext.builder().build())
	}


// Decodes the value of `fieldName` from a document produced by [encodeFieldBytes] or a hand-written golden document.
// [arguments] threads explicit type arguments (e.g. the element type of a `List<T>`) through the public
// `CodecEx` decode overload, exercising the same path as a typed `reader.value<List<T>>()` call would.
internal fun <Value : Any> Codec<Value>.decodeFieldValue(
	bytes: ByteArray,
	fieldName: String,
	arguments: List<KTypeProjection>? = null,
): Value =
	readDocument(bytes) {
		check(readName() == fieldName) { "Expected field name '$fieldName'." }

		when (val codec = this@decodeFieldValue) {
			is CodecEx<Value> -> codec.decode(this, DecoderContext.builder().build(), arguments)
			else -> codec.decode(this, DecoderContext.builder().build())
		}
	}
