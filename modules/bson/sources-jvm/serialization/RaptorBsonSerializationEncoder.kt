package io.fluidsonic.raptor.bson

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import org.bson.*


/**
 * The escape hatch a [KSerializer] uses to write a BSON representation that no `kotlinx.serialization`
 * primitive kind can express — a BSON DateTime, ObjectId or binary value, for instance.
 *
 * Every [Encoder] handed to a serializer by [RaptorBsonSerializationCodec] implements this interface, so a
 * serializer needing raw access casts to it:
 *
 * ```
 * override fun serialize(encoder: Encoder, value: Timestamp) {
 *     val bsonEncoder = encoder as? RaptorBsonSerializationEncoder
 *         ?: throw SerializationException("…")
 *
 *     bsonEncoder.bsonWriter.writeDateTime(value.toEpochMilliseconds())
 * }
 * ```
 *
 * A serializer taking this route must write exactly one BSON value to [bsonWriter] and must not touch the
 * surrounding document or array structure — the field name, if any, has already been written.
 */
internal interface RaptorBsonSerializationEncoder : Encoder {

	/**
	 * The underlying writer, positioned where the value is to be written.
	 */
	val bsonWriter: BsonWriter
}
