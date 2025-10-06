package io.fluidsonic.raptor.mongo2

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import org.bson.*
import org.bson.types.*


public interface MongoBsonReader {

	public fun asScope(): MongoBsonReaderScope
	public fun boolean(): Boolean
	public fun byteArray(): ByteArray
	public fun bsonType(): BsonType
	public fun double(): Double
	public fun endArray()
	public fun endDocument()
	public fun fieldName(): String
	public fun int(): Int
	public fun long(): Long
	public fun nextBsonType(): BsonType
	public fun nullValue(): Nothing?
	public fun objectId(): ObjectId
	public fun skipValue()
	public fun startArray()
	public fun startDocument()
	public fun string(): String
	public fun timestamp(): Timestamp
}


public inline operator fun <R> MongoBsonReader.invoke(action: MongoBsonReaderScope.() -> R): R =
	with(asScope(), action)


@RaptorInternalApi
public fun MongoBsonReader.asLegacy(): BsonReader? =
	(this as? LegacyMongoBsonReader)?.legacy
