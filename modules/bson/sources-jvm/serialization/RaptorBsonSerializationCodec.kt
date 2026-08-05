package io.fluidsonic.raptor.bson

import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import org.bson.*
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext


/**
 * **Internal and experimental — not public API in this release.** What holds it back is wiring, not the codec
 * itself: every call site has to hand in its own [SerializersModule], because Raptor offers no assembly-time
 * injection point for one the way it does for the `codecRegistry` that every codec gets for free. Going public
 * waits on first-class Raptor support for composing `SerializersModule`s across plugins, mirroring how the
 * `RaptorBsonDefinition`s contributed by several plugins already merge into a single `codecRegistry` today.
 *
 * A `org.bson.codecs.Codec` that reads and writes a `kotlinx.serialization`-serializable [Value] directly
 * through [BsonReader]/[BsonWriter], without involving `org.mongodb:bson-kotlinx`.
 *
 * This is the second of Raptor's two ways to give a type a BSON representation, the other being a hand-written
 * [definition] block. Both produce the identical wire format for the identical record shape; the choice is
 * between deriving the field list from [Value]'s `@Serializable` declaration and spelling it out. Registering
 * this codec with Raptor normally goes through [serializableDefinition], which builds it and wraps it into a
 * `RaptorBsonDefinition` in one call.
 *
 * Document fields are matched **by name** on decode, in whatever order they appear on the wire, and fields
 * the [serializer] does not know are skipped — so a document written by an *older* version of [Value] (with
 * fields the current [Value] no longer has) still decodes. Reading a document written by a *newer* version of
 * [Value] (with properties the current [serializer] doesn't have) is not this format's business either way —
 * an absent property falls back to its Kotlin default if it has one, or `kotlinx.serialization` itself throws
 * [MissingFieldException] when it doesn't.
 *
 * Supported out of the box are `Int`, `Long`, `Double`, `Boolean` and `String` properties, nullable
 * properties, `@JvmInline value class` properties (encoded as their wrapped scalar), nested serializable
 * classes (encoded as a BSON document) and lists (encoded as a BSON array). Anything else — `Byte`, `Char`,
 * `Short`, `Float`, enums, maps and polymorphism — throws [SerializationException]; a type needing another
 * BSON representation must supply a custom [KSerializer] that reaches for
 * [RaptorBsonSerializationEncoder]/[RaptorBsonSerializationDecoder].
 *
 * @param valueClass The class reported as [getEncoderClass], which is what a `CodecRegistry` and
 *   `RaptorBsonDefinition.of(Codec)` match on — it must be [Value]'s own class, not a supertype.
 * @param serializer The serializer used for both directions. Its descriptor determines the field names and
 *   the BSON types written.
 * @param serializersModule The module handed to the encoder and decoder, used to resolve contextual and
 *   polymorphic serializers. Pass `EmptySerializersModule()` when [Value] needs neither.
 * @param preserveNulls Whether a null property is written as an explicit BSON null (`true`) or the field is
 *   omitted from the document entirely (`false`, matching `writer.value(field, value)` in every hand-written
 *   Raptor codec). Note that omitting is not free on the decode side: `kotlinx.serialization` cannot tell an
 *   omitted field from one that was never written, so a nullable property must declare a Kotlin default
 *   (`= null`) to survive a round trip — otherwise decoding throws [MissingFieldException]. Null *array*
 *   elements are always written explicitly regardless of this setting, since a BSON array element's name is
 *   its position.
 */
internal class RaptorBsonSerializationCodec<Value : Any>(
	private val valueClass: Class<Value>,
	private val serializer: KSerializer<Value>,
	private val serializersModule: SerializersModule,
	private val preserveNulls: Boolean = false,
) : Codec<Value> {

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Value =
		serializer.deserialize(DefaultBsonSerializationDecoder(reader = reader, serializersModule = serializersModule))


	override fun encode(writer: BsonWriter, value: Value, encoderContext: EncoderContext) {
		serializer.serialize(
			DefaultBsonSerializationEncoder(writer = writer, serializersModule = serializersModule, preserveNulls = preserveNulls),
			value,
		)
	}


	override fun getEncoderClass(): Class<Value> =
		valueClass


	internal companion object {

		/**
		 * Creates a codec for [Value] using the serializer `kotlinx.serialization` resolves for it, which
		 * requires [Value] to be annotated `@Serializable` or covered by [serializersModule].
		 *
		 * See the class documentation for [serializersModule] and [preserveNulls].
		 *
		 * @throws SerializationException if no serializer can be resolved for [Value].
		 */
		internal inline fun <reified Value : Any> of(
			serializersModule: SerializersModule,
			preserveNulls: Boolean = false,
		): RaptorBsonSerializationCodec<Value> =
			RaptorBsonSerializationCodec(
				valueClass = Value::class.java,
				serializer = serializersModule.serializer<Value>(),
				serializersModule = serializersModule,
				preserveNulls = preserveNulls,
			)
	}
}
