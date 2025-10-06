package io.fluidsonic.raptor.mongo2

import io.fluidsonic.time.*
import org.bson.*
import org.bson.types.*


internal class LegacyMongoBsonWriter(
	val legacy: BsonWriter,
) : MongoBsonWriter, MongoBsonWriterScope {

	override val bsonWriter: MongoBsonWriter
		get() = this


	override fun asScope(): MongoBsonWriterScope =
		this


	override fun boolean(value: Boolean) {
		legacy.writeBoolean(value)
	}


	override fun byteArray(value: ByteArray) {
		legacy.writeBinaryData(BsonBinary(value))
	}


	override fun double(value: Double) {
		legacy.writeDouble(value)
	}


	override fun endArray() {
		legacy.writeEndArray()
	}


	override fun endDocument() {
		legacy.writeEndDocument()
	}


	override fun fieldName(name: String) {
		legacy.writeName(name)
	}


	override fun int(value: Int) {
		legacy.writeInt32(value)
	}


	override fun long(value: Long) {
		legacy.writeInt64(value)
	}


	override fun nullValue() {
		legacy.writeNull()
	}


	override fun objectId(value: ObjectId) {
		legacy.writeObjectId(value)
	}


	override fun startArray() {
		legacy.writeStartArray()
	}


	override fun startDocument() {
		legacy.writeStartDocument()
	}


	override fun string(value: String) {
		legacy.writeString(value)
	}


	override fun timestamp(value: Timestamp) {
		legacy.writeDateTime(value.toEpochMilliseconds())
	}
}
