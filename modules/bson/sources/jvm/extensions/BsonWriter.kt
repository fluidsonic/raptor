package io.fluidsonic.raptor

import org.bson.*


public fun BsonWriter.write(name: String, value: Boolean) {
	writeName(name)
	writeBoolean(value)
}


public fun BsonWriter.write(name: String, value: Boolean?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	writeName(name)

	if (value != null) writeBoolean(value)
	else writeNull()
}

public inline fun BsonWriter.write(name: String, write: BsonWriter.() -> Unit) {
	writeName(name)
	writeDocument(write = write)
}


public fun BsonWriter.write(name: String, value: Double) {
	writeName(name)
	writeDouble(value)
}


public fun BsonWriter.write(name: String, value: Double?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	writeName(name)

	if (value != null) writeDouble(value)
	else writeNull()
}


public fun BsonWriter.write(name: String, value: Int) {
	writeName(name)
	writeInt32(value)
}


public fun BsonWriter.write(name: String, value: Int?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	writeName(name)

	if (value != null) writeInt32(value)
	else writeNull()
}


public fun BsonWriter.write(name: String, value: Long) {
	writeName(name)
	writeInt64(value)
}


public fun BsonWriter.write(name: String, value: Long?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	writeName(name)

	if (value != null) writeInt64(value)
	else writeNull()
}


public fun BsonWriter.write(name: String, value: String) {
	writeName(name)
	writeString(value)
}


public fun BsonWriter.write(name: String, value: String?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	writeName(name)

	if (value != null) writeString(value)
	else writeNull()
}


public inline fun BsonWriter.writeArray(name: String, write: BsonWriter.() -> Unit) {
	writeName(name)
	writeArray(write)
}


public inline fun BsonWriter.writeArray(write: BsonWriter.() -> Unit) {
	writeStartArray()
	write()
	writeEndArray()
}


public inline fun BsonWriter.writeDocument(write: BsonWriter.() -> Unit) {
	writeStartDocument()
	write()
	writeEndDocument()
}


public inline fun <Value : Any> BsonWriter.writeDocument(document: Value, write: BsonWriter.(value: Value) -> Unit) {
	writeDocument {
		write(document)
	}
}


public inline fun <Key, Value> BsonWriter.writeMap(map: Map<Key, Value>, writeEntry: BsonWriter.(key: Key, value: Value) -> Unit) {
	writeDocument {
		for ((key, value) in map) {
			writeEntry(key, value)
		}
	}
}


public fun BsonWriter.writeStrings(strings: Iterable<String>) {
	writeStartArray()
	for (string in strings) {
		writeString(string)
	}
	writeEndArray()
}
