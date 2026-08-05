package tests

import io.fluidsonic.country.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.time.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*
// Deliberately not a star import: `org.bson.codecs.Encoder`/`Decoder` would collide with
// `kotlinx.serialization.encoding.Encoder`/`Decoder`, which the `KSerializer`s below implement against.
import org.bson.codecs.Codec


// TEST FIXTURE — not wired into any production path. Nothing here is referenced by
// `RaptorBsonDefinition.raptorDefaults` or by any existing type's definition; the declarations exist so
// `SerializationCodecBsonTests` can hold `RaptorBsonSerializationCodec` — Raptor's
// `kotlinx.serialization`-based way of writing a BSON codec — against hand-written Raptor codecs for
// identical record shapes, field name for field name and byte for byte.
//
// Each record here is deliberately minimal, isolating one aspect of the format: primitives, nullability,
// nesting, a custom `KSerializer`, a Kotlin property default. `MinimalWideDocument` at the bottom is the one
// exception — it combines several of them, mirroring `WideDocument` in `WideDocumentModel.kt` field for field.


@Serializable
internal data class MinimalPrimitiveDocument(
	val a: Int,
	val b: Long,
	val c: Double,
	val d: Boolean,
	val e: String,
)


internal val minimalPrimitiveDocumentCodec: Codec<MinimalPrimitiveDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())


internal val minimalPrimitiveDocumentHandWrittenDefinition: RaptorBsonDefinition =
	raptor.bson.definition<MinimalPrimitiveDocument> {
		decode {
			reader.document {
				MinimalPrimitiveDocument(
					a = value<Int>("a"),
					b = value<Long>("b"),
					c = value<Double>("c"),
					d = value<Boolean>("d"),
					e = value<String>("e"),
				)
			}
		}

		encode { document ->
			writer.document {
				value("a", document.a)
				value("b", document.b)
				value("c", document.c)
				value("d", document.d)
				value("e", document.e)
			}
		}
	}


internal val minimalPrimitiveDocumentSample: MinimalPrimitiveDocument =
	MinimalPrimitiveDocument(
		a = 42,
		b = 42_000_000_000L,
		c = 3.14159,
		d = true,
		e = "hello world",
	)


// A single-nullable-field record, the smallest shape that makes null handling observable at all. It
// deliberately declares no Kotlin default, which is what exposes the decode-side consequence of omitting the
// field: an omitted field is indistinguishable from one that was never written, so
// `MinimalDefaultedNullableDocument` below is the shape that actually survives a round trip.
@Serializable
internal data class MinimalNullableDocument(
	val value: String?,
)


// Default `preserveNulls = false`, matching `writer.value(field, value)` in every hand-written Raptor codec.
internal val minimalNullableDocumentCodec: Codec<MinimalNullableDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())


internal val minimalNullableDocumentPreservingNullCodec: Codec<MinimalNullableDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule(), preserveNulls = true)


// The same shape with a Kotlin default, which is what a record must declare for an omitted null field to
// decode back to null instead of failing with `MissingFieldException`.
@Serializable
internal data class MinimalDefaultedNullableDocument(
	val value: String? = null,
)


internal val minimalDefaultedNullableDocumentCodec: Codec<MinimalDefaultedNullableDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())


// A null *array element* can never be omitted the way a document field can — dropping it would shift every
// later element's position — so this record pins that `preserveNulls` does not reach inside a BSON array.
@Serializable
internal data class MinimalNullableElementListDocument(
	val values: List<String?>,
)


internal val minimalNullableElementListDocumentCodec: Codec<MinimalNullableElementListDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())


// Exercises the two structure shapes beyond a flat document: a BSON array (`tags`) and a nested BSON
// document (`inner`). `rows` pins the array-element counter across nesting — a single counter field would
// decode the outer list wrongly once an inner list has advanced it.
@Serializable
internal data class MinimalNestedDocument(
	val name: String,
	val tags: List<String>,
	val rows: List<List<Int>>,
	val inner: MinimalPrimitiveDocument,
)


internal val minimalNestedDocumentCodec: Codec<MinimalNestedDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())


internal val minimalNestedDocumentSample: MinimalNestedDocument =
	MinimalNestedDocument(
		name = "outer",
		tags = listOf("a", "b", "c"),
		rows = listOf(listOf(1, 2), emptyList(), listOf(3)),
		inner = minimalPrimitiveDocumentSample,
	)


internal val minimalNullableDocumentHandWrittenDefinition: RaptorBsonDefinition =
	raptor.bson.definition<MinimalNullableDocument> {
		decode {
			reader.document {
				MinimalNullableDocument(value = value<String?>("value"))
			}
		}

		encode { document ->
			writer.document {
				value("value", document.value, preserveNull = true)
			}
		}
	}


// The same hand-written definition without `preserveNull`, i.e. with Raptor's default null handling, so the
// serialization codec's own default is byte-comparable against it.
internal val minimalNullableDocumentOmittingNullHandWrittenDefinition: RaptorBsonDefinition =
	raptor.bson.definition<MinimalNullableDocument> {
		decode {
			reader.document {
				MinimalNullableDocument(value = value<String?>("value"))
			}
		}

		encode { document ->
			writer.document {
				value("value", document.value)
			}
		}
	}


/**
 * Serializes a [Timestamp] as a BSON DateTime of [Timestamp.toEpochMilliseconds], reproducing the wire
 * format of `Timestamp.bsonDefinition()` exactly.
 *
 * Reaches for Raptor's own escape hatch — [RaptorBsonSerializationDecoder.bsonReader] /
 * [RaptorBsonSerializationEncoder.bsonWriter] — to prove that a Raptor-specific wire format no
 * `kotlinx.serialization` primitive kind can express (a BSON DateTime) is still reachable from the
 * from-scratch format.
 */
// The declared `PrimitiveKind` never reaches the wire: both methods below bypass kind-driven dispatch
// entirely. `STRING` mirrors what MongoDB's own `InstantAsBsonDateTime` declares for the identical trick.
@OptIn(ExperimentalSerializationApi::class)
internal object MinimalTimestampAsBsonDateTime : KSerializer<Timestamp> {

	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("MinimalTimestampAsBsonDateTime", PrimitiveKind.STRING)


	override fun deserialize(decoder: Decoder): Timestamp {
		val bsonDecoder = decoder as? RaptorBsonSerializationDecoder
			?: throw SerializationException("`Timestamp` can only be deserialized from BSON, not by ${decoder::class}.")

		return Timestamp.fromEpochMilliseconds(bsonDecoder.bsonReader.readDateTime())
	}


	override fun serialize(encoder: Encoder, value: Timestamp) {
		val bsonEncoder = encoder as? RaptorBsonSerializationEncoder
			?: throw SerializationException("`Timestamp` can only be serialized to BSON, not by ${encoder::class}.")

		bsonEncoder.bsonWriter.writeDateTime(value.toEpochMilliseconds())
	}
}


/**
 * Serializes a [Country] as its [CountryCode] string, reproducing the wire format of
 * `Country.bsonDefinition()` exactly.
 */
// Unlike [MinimalTimestampAsBsonDateTime] this needs no BSON escape hatch: a `PrimitiveKind.STRING`
// descriptor with `encodeString`/`decodeString` — the same idiom as `RaptorEntityId.Typed.Definition` in
// `modules/entities-core` — already lands on a BSON string, which makes it format-agnostic.
internal object CountryAsBsonString : KSerializer<Country> {

	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("CountryAsBsonString", PrimitiveKind.STRING)


	override fun deserialize(decoder: Decoder): Country =
		Country.forCode(CountryCode.parse(decoder.decodeString()))


	override fun serialize(encoder: Encoder, value: Country) {
		encoder.encodeString(value.code.toString())
	}
}


// Field-for-field the same shape, names, order and values as `WideDocument` in `WideDocumentModel.kt`, so
// the two codecs' encoded bytes are directly comparable.
@Serializable
internal data class MinimalWideDocument(
	val a: Int,
	val b: Long,
	val c: Double,
	val d: Boolean,
	val e: String,
	@Serializable(with = MinimalTimestampAsBsonDateTime::class) val f: Timestamp,
	@Serializable(with = CountryAsBsonString::class) val g: Country,
	val h: List<String>,
)


internal val minimalWideDocumentCodec: Codec<MinimalWideDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())


// The from-scratch codec, wrapped for Raptor via the existing `RaptorBsonDefinition.of(Codec)`, so the
// benchmark rows differ only in the codec inside.
internal val minimalWideDocumentDefinition: RaptorBsonDefinition =
	RaptorBsonDefinition.of(minimalWideDocumentCodec)


// The existing-style hand-written equivalent for the very same class, so both codecs can be fed the
// identical instance and their bytes compared directly.
internal val minimalWideDocumentHandWrittenDefinition: RaptorBsonDefinition =
	raptor.bson.definition<MinimalWideDocument> {
		decode {
			reader.document {
				MinimalWideDocument(
					a = value<Int>("a"),
					b = value<Long>("b"),
					c = value<Double>("c"),
					d = value<Boolean>("d"),
					e = value<String>("e"),
					f = value<Timestamp>("f"),
					g = value<Country>("g"),
					h = value<List<String>>("h"),
				)
			}
		}

		encode { document ->
			writer.document {
				value("a", document.a)
				value("b", document.b)
				value("c", document.c)
				value("d", document.d)
				value("e", document.e)
				value("f", document.f)
				value("g", document.g)
				value("h", document.h)
			}
		}
	}


// Missing-field handling is deliberately not the format's business: `decodeElementIndex` just reports
// `DECODE_DONE` and the generated deserializer decides between the Kotlin default and a hard failure.
@Serializable
internal data class MinimalDefaultingDocument(
	val required: Int,
	val optional: String = "fallback",
)


internal val minimalDefaultingDocumentCodec: Codec<MinimalDefaultingDocument> =
	RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())


// Root types that are not themselves `@Serializable` must still resolve through the module the caller passed
// to `of(serializersModule = …)` — this type has no compile-time serializer at all, so encoding it only works
// if `of` resolves the root serializer through the module rather than through the format-agnostic top-level
// `serializer<Value>()`, which knows nothing about a caller-supplied `SerializersModule`.
internal data class ContextualRootProbe(val value: String)


// A BSON top-level value must be a document, not a bare scalar, so this writes a one-field document rather
// than reusing the bare-string trick `MinimalTimestampAsBsonDateTime`/`CountryAsBsonString` use for a
// *nested* property.
@OptIn(ExperimentalSerializationApi::class)
internal object ContextualRootProbeSerializer : KSerializer<ContextualRootProbe> {

	override val descriptor: SerialDescriptor =
		buildClassSerialDescriptor("ContextualRootProbe") {
			element<String>("value")
		}


	override fun deserialize(decoder: Decoder): ContextualRootProbe {
		val composite = decoder.beginStructure(descriptor)
		var value = ""

		while (true) {
			when (val index = composite.decodeElementIndex(descriptor)) {
				0 -> value = composite.decodeStringElement(descriptor, index)
				CompositeDecoder.DECODE_DONE -> break
				else -> error("Unexpected index: $index")
			}
		}

		composite.endStructure(descriptor)

		return ContextualRootProbe(value)
	}


	override fun serialize(encoder: Encoder, value: ContextualRootProbe) {
		val composite = encoder.beginStructure(descriptor)
		composite.encodeStringElement(descriptor, 0, value.value)
		composite.endStructure(descriptor)
	}
}


internal val contextualRootProbeModule: SerializersModule =
	SerializersModule { contextual(ContextualRootProbe::class, ContextualRootProbeSerializer) }


internal val minimalWideDocumentSample: MinimalWideDocument =
	MinimalWideDocument(
		a = 42,
		b = 42_000_000_000L,
		c = 3.14159,
		d = true,
		e = "hello world",
		f = Timestamp.fromEpochMilliseconds(1_705_314_600_000L),
		g = Country.forCode(CountryCode.parse("DE")),
		h = listOf("a", "b", "c"),
	)
