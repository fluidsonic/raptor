package io.fluidsonic.raptor

import org.bson.*
import org.bson.AbstractBsonReader.*


fun BsonReader.expectValue(methodName: String) {
	if (this !is AbstractBsonReader) {
		return
	}

	var state = this.state
	if (state == State.INITIAL || state == State.SCOPE_DOCUMENT || state == State.TYPE) {
		readBsonType()
	}

	state = this.state
	if (state == State.NAME) {
		skipName()
	}

	state = this.state
	if (state != State.VALUE) {
		throw BsonInvalidOperationException("$methodName can only be called when State is ${State.VALUE}, not when State is $state.")
	}
}


inline fun <T> BsonReader.readArray(name: String, read: BsonReader.() -> T): T {
	readName(name)

	return readArray(read)
}


inline fun <T> BsonReader.readArray(read: BsonReader.() -> T): T {
	readStartArray()
	val result = read()
	readEndArray()

	return result
}


inline fun BsonReader.readArrayWithValues(readValue: BsonReader.() -> Unit) {
	readArray {
		while (readBsonType() != BsonType.END_OF_DOCUMENT) {
			readValue()
		}
	}
}


fun BsonReader.readBooleanOrNull(): Boolean? {
	expectValue("readBooleanOrNull")

	if (currentBsonType == BsonType.NULL) {
		skipValue()
		return null
	}

	return readBoolean()
}


fun BsonReader.readCoercedDouble(): Double {
	expectValue("readCoercedDouble")

	return when (currentBsonType) {
		BsonType.INT32 -> readInt32().toDouble()
		BsonType.INT64 -> readInt64().toDouble()
		else -> readDouble()
	}
}


fun BsonReader.readCoercedInt32(): Int {
	expectValue("readCoercedInt32")

	return when (currentBsonType) {
		BsonType.DOUBLE -> readDouble().toInt()
		BsonType.INT64 -> readInt64().toInt()
		else -> readInt32()
	}
}


fun BsonReader.readCoercedInt32OrNull(): Int? {
	expectValue("readCoercedInt32OrNull")

	if (currentBsonType == BsonType.NULL) {
		skipValue()
		return null
	}

	return readCoercedInt32()
}


inline fun <T> BsonReader.readDocument(read: BsonReader.() -> T): T {
	readStartDocument()
	val result = read()
	readEndDocument()

	return result
}


inline fun <T> BsonReader.readDocument(name: String, read: BsonReader.() -> T): T {
	readName(name)

	return readDocument(read)
}


inline fun <T> BsonReader.readDocumentOrNull(read: BsonReader.() -> T): T? {
	expectValue("readDocumentOrNull")

	if (currentBsonType == BsonType.NULL) {
		skipValue()
		return null
	}

	return readDocument(read)
}


inline fun <T> BsonReader.readDocumentOrNull(name: String, read: BsonReader.() -> T): T? {
	readName(name)

	return readDocumentOrNull(read)
}


inline fun BsonReader.readDocumentWithValues(readValue: BsonReader.(fieldName: String) -> Unit) {
	readDocument {
		while (readBsonType() != BsonType.END_OF_DOCUMENT) {
			readValue(readName())
		}
	}
}


fun BsonReader.readInt32OrNull(): Int? {
	expectValue("readInt32OrNull")

	if (currentBsonType == BsonType.NULL) {
		skipValue()
		return null
	}

	return readInt32()
}


inline fun <Key, Value> BsonReader.readMap(readEntry: BsonReader.(fieldName: String) -> Pair<Key, Value>): Map<Key, Value> {
	val map = mutableMapOf<Key, Value>()
	readDocumentWithValues { fieldName ->
		map += readEntry(fieldName)
	}
	return map
}


fun BsonReader.readStringOrNull(): String? {
	expectValue("readStringOrNull")

	if (currentBsonType == BsonType.NULL) {
		skipValue()
		return null
	}

	return readString()
}
