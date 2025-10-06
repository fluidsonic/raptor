package io.fluidsonic.raptor.mongo2

import io.fluidsonic.time.*
import org.bson.*
import org.bson.types.*


internal class LegacyMongoBsonReader(
	val legacy: BsonReader,
) : MongoBsonReader, MongoBsonReaderScope {

	override val bsonReader: MongoBsonReader
		get() = this


	override fun asScope(): MongoBsonReaderScope =
		this


	override fun boolean(): Boolean =
		legacy.readBoolean()


	override fun bsonType(): BsonType =
		legacy.currentBsonType


	override fun byteArray(): ByteArray =
		legacy.readBinaryData().data


	override fun double(): Double =
		legacy.readDouble()


	override fun endArray() {
		legacy.readEndArray()
	}


	override fun endDocument() {
		legacy.readEndDocument()
	}


	override fun fieldName(): String =
		legacy.readName()


	override fun int(): Int =
		legacy.readInt32()


	override fun long(): Long =
		when (legacy.currentBsonType) {
			BsonType.INT32 -> legacy.readInt32().toLong()
			else -> legacy.readInt64()
		}


	override fun nullValue(): Nothing? {
		legacy.readNull()

		return null
	}


	override fun nextBsonType(): BsonType =
		legacy.readBsonType()


	override fun objectId(): ObjectId =
		legacy.readObjectId()


	override fun skipValue() {
		legacy.skipValue()
	}


	override fun startArray() {
		legacy.readStartArray()
	}


	override fun startDocument() {
		legacy.readStartDocument()
	}


	override fun string(): String =
		legacy.readString()


	override fun timestamp(): Timestamp =
		Timestamp.fromEpochMilliseconds(legacy.readDateTime())
}
