package io.fluidsonic.raptor.bson.internal

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlin.reflect.*
import org.bson.*
import org.bson.types.*


internal class DefaultBsonWriterScope(
	parent: RaptorBsonScope,
	writer: BsonWriter,
) : RaptorBsonWriterScope, RaptorBsonWriter, RaptorBsonScope by parent, BsonWriter by writer {

	override fun endArray() {
		writeEndArray()
	}


	override fun endDocument() {
		writeEndDocument()
	}


	override fun fieldName(name: String) {
		writeName(name)
	}


	override fun internal(): BsonWriter =
		this


	override fun startArray() {
		writeStartArray()
	}


	override fun startDocument() {
		writeStartDocument()
	}


	override fun value(value: Any?) {
		if (value == null) {
			writeNull()
			return
		}

		valueAs(value, valueClass = value::class)
	}


	override fun value(value: Boolean) {
		writeBoolean(value)
	}


	override fun value(value: Double) {
		writeDouble(value)
	}


	override fun value(value: Float) {
		writeDouble(value.toDouble())
	}


	override fun value(value: Int) {
		writeInt32(value)
	}


	override fun value(value: Long) {
		writeInt64(value)
	}


	override fun value(value: Nothing?) {
		writeNull()
	}


	override fun value(value: ObjectId) {
		writeObjectId(value)
	}


	override fun value(value: Short) {
		writeInt32(value.toInt())
	}


	override fun value(value: String) {
		writeString(value)
	}


	override fun value(value: Timestamp) {
		writeDateTime(value.toEpochMilliseconds())
	}


	override fun <Value : Any> valueAs(value: Value?, valueClass: KClass<out Value>) {
		if (value == null) {
			writeNull()
			return
		}

		codecRegistry.encode(scope = this, value = value, valueClass = valueClass)
	}


	override val writer: RaptorBsonWriter
		get() = this
}
