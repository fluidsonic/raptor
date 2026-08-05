package io.fluidsonic.raptor.bson

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import org.bson.*


/**
 * The escape hatch a [KSerializer] uses to read a BSON representation that no `kotlinx.serialization`
 * primitive kind can express — a BSON DateTime, ObjectId or binary value, for instance.
 *
 * Every [Decoder] handed to a serializer by [RaptorBsonSerializationCodec] implements this interface, so a
 * serializer needing raw access casts to it:
 *
 * ```
 * override fun deserialize(decoder: Decoder): Timestamp {
 *     val bsonDecoder = decoder as? RaptorBsonSerializationDecoder
 *         ?: throw SerializationException("…")
 *
 *     return Timestamp.fromEpochMilliseconds(bsonDecoder.bsonReader.readDateTime())
 * }
 * ```
 *
 * A serializer taking this route must consume exactly one BSON value from [bsonReader] and must not touch
 * the surrounding document or array structure.
 */
internal interface RaptorBsonSerializationDecoder : Decoder {

	/**
	 * The underlying reader, positioned on the value to be decoded — its field name, if any, has already
	 * been consumed.
	 */
	val bsonReader: BsonReader
}
