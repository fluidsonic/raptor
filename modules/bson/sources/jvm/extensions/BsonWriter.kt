package io.fluidsonic.raptor

import org.bson.*


// FIXME consistency

public fun BsonWriter.write(name: String, value: Boolean) {
	writeName(name)
	writeBoolean(value)
}


public fun BsonWriter.write(name: String, value: Boolean?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	write(name = name, value = value)
}


public inline fun BsonWriter.write(name: String, write: BsonWriter.() -> Unit) {
	writeName(name)
	writeDocument(write = write)
}


public inline fun <Value : Any> BsonWriter.write(name: String, document: Value, write: BsonWriter.(value: Value) -> Unit) {
	writeName(name)
	writeDocument(document = document, write = write)
}


@JvmName("writeOrSkip")
public inline fun <Value : Any> BsonWriter.write(name: String, documentOrSkip: Value?, write: BsonWriter.(value: Value) -> Unit) {
	documentOrSkip ?: return

	write(name = name, document = documentOrSkip, write = write)
}


public fun BsonWriter.write(name: String, double: Double) {
	writeName(name)
	writeDouble(double)
}


@JvmName("writeOrSkip")
public fun BsonWriter.write(name: String, doubleOrSkip: Double?) {
	if (doubleOrSkip == null) {
		return
	}

	write(name = name, double = doubleOrSkip)
}


public fun BsonWriter.write(name: String, int32: Int) {
	writeName(name)
	writeInt32(int32)
}


@JvmName("writeOrSkip")
public fun BsonWriter.write(name: String, int32OrSkip: Int?) {
	if (int32OrSkip == null) {
		return
	}

	write(name = name, int32 = int32OrSkip)
}


public fun BsonWriter.write(name: String, string: String) {
	writeName(name)
	writeString(string)
}


public fun BsonWriter.write(name: String, strings: Iterable<String>) {
	writeName(name)
	writeStrings(strings)
}


public inline fun <K, V> BsonWriter.write(name: String, value: Map<K, V>, writeEntry: (entry: Map.Entry<K, V>) -> Unit) {
	writeName(name)
	writeDocument {
		value.entries.forEach(writeEntry)
	}
}


@JvmName("writeOrSkip")
public inline fun <K, V> BsonWriter.write(name: String, valueOrSkip: Map<K, V>?, writeEntry: (entry: Map.Entry<K, V>) -> Unit) {
	valueOrSkip ?: return

	write(name = name, value = valueOrSkip, writeEntry = writeEntry)
}


public fun BsonWriter.write(name: String, stringOrSkip: String?, skipIfEmpty: Boolean = false) {
	if (stringOrSkip == null || (skipIfEmpty && stringOrSkip.isEmpty())) {
		return
	}

	write(name = name, string = stringOrSkip)
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
