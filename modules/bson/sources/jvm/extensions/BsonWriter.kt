package io.fluidsonic.raptor

import org.bson.*


// FIXME consistency

fun BsonWriter.write(name: String, value: Boolean) {
	writeName(name)
	writeBoolean(value)
}


fun BsonWriter.write(name: String, value: Boolean?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	write(name = name, value = value)
}


inline fun BsonWriter.write(name: String, write: BsonWriter.() -> Unit) {
	writeName(name)
	writeDocument(write = write)
}


inline fun <Value : Any> BsonWriter.write(name: String, document: Value, write: BsonWriter.(value: Value) -> Unit) {
	writeName(name)
	writeDocument(document = document, write = write)
}


@JvmName("writeOrSkip")
inline fun <Value : Any> BsonWriter.write(name: String, documentOrSkip: Value?, write: BsonWriter.(value: Value) -> Unit) {
	documentOrSkip ?: return

	write(name = name, document = documentOrSkip, write = write)
}


fun BsonWriter.write(name: String, double: Double) {
	writeName(name)
	writeDouble(double)
}


@JvmName("writeOrSkip")
fun BsonWriter.write(name: String, doubleOrSkip: Double?) {
	if (doubleOrSkip == null) {
		return
	}

	write(name = name, double = doubleOrSkip)
}


fun BsonWriter.write(name: String, int32: Int) {
	writeName(name)
	writeInt32(int32)
}


@JvmName("writeOrSkip")
fun BsonWriter.write(name: String, int32OrSkip: Int?) {
	if (int32OrSkip == null) {
		return
	}

	write(name = name, int32 = int32OrSkip)
}


fun BsonWriter.write(name: String, string: String) {
	writeName(name)
	writeString(string)
}


fun BsonWriter.write(name: String, strings: Iterable<String>) {
	writeName(name)
	writeStrings(strings)
}


inline fun <K, V> BsonWriter.write(name: String, value: Map<K, V>, writeEntry: (entry: Map.Entry<K, V>) -> Unit) {
	writeName(name)
	writeDocument {
		value.entries.forEach(writeEntry)
	}
}


@JvmName("writeOrSkip")
inline fun <K, V> BsonWriter.write(name: String, valueOrSkip: Map<K, V>?, writeEntry: (entry: Map.Entry<K, V>) -> Unit) {
	valueOrSkip ?: return

	write(name = name, value = valueOrSkip, writeEntry = writeEntry)
}


fun BsonWriter.write(name: String, stringOrSkip: String?, skipIfEmpty: Boolean = false) {
	if (stringOrSkip == null || (skipIfEmpty && stringOrSkip.isEmpty())) {
		return
	}

	write(name = name, string = stringOrSkip)
}


inline fun BsonWriter.writeArray(name: String, write: BsonWriter.() -> Unit) {
	writeName(name)
	writeArray(write)
}


inline fun BsonWriter.writeArray(write: BsonWriter.() -> Unit) {
	writeStartArray()
	write()
	writeEndArray()
}


inline fun BsonWriter.writeDocument(write: BsonWriter.() -> Unit) {
	writeStartDocument()
	write()
	writeEndDocument()
}


inline fun <Value : Any> BsonWriter.writeDocument(document: Value, write: BsonWriter.(value: Value) -> Unit) {
	writeDocument {
		write(document)
	}
}


inline fun <Key, Value> BsonWriter.writeMap(map: Map<Key, Value>, writeEntry: BsonWriter.(key: Key, value: Value) -> Unit) {
	writeDocument {
		for ((key, value) in map) {
			writeEntry(key, value)
		}
	}
}


fun BsonWriter.writeStrings(strings: Iterable<String>) {
	writeStartArray()
	for (string in strings) {
		writeString(string)
	}
	writeEndArray()
}
