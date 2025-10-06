package io.fluidsonic.raptor.mongo2

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import org.bson.*
import org.bson.types.*


public interface MongoBsonWriter {

	public fun asScope(): MongoBsonWriterScope
	public fun boolean(value: Boolean)
	public fun byteArray(value: ByteArray)
	public fun double(value: Double)
	public fun endArray()
	public fun endDocument()
	public fun fieldName(name: String)
	public fun int(value: Int)
	public fun long(value: Long)
	public fun nullValue()
	public fun objectId(value: ObjectId)
	public fun startArray()
	public fun startDocument()
	public fun string(value: String)
	public fun timestamp(value: Timestamp)
}


public inline operator fun <R> MongoBsonWriter.invoke(action: MongoBsonWriterScope.() -> R): R =
	with(asScope(), action)


@RaptorInternalApi
public fun MongoBsonWriter.asLegacy(): BsonWriter? =
	(this as? LegacyMongoBsonWriter)?.legacy
