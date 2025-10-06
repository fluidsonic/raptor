package io.fluidsonic.raptor.mongo2

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlin.contracts.*
import org.bson.*
import org.bson.types.*


public interface MongoBsonReaderScope {

	@RaptorDsl
	public val bsonReader: MongoBsonReader
}


@RaptorDsl
public fun MongoBsonReaderScope.boolean(): Boolean =
	bsonReader.boolean()


@RaptorDsl
public fun MongoBsonReaderScope.byteArray(): ByteArray =
	bsonReader.byteArray()


@RaptorDsl
public fun MongoBsonReaderScope.bsonType(): BsonType =
	bsonReader.bsonType()


@RaptorDsl
public fun MongoBsonReaderScope.double(): Double =
	bsonReader.double()


@RaptorDsl
public fun MongoBsonReaderScope.endArray() {
	bsonReader.endArray()
}


@RaptorDsl
public fun MongoBsonReaderScope.endDocument() {
	bsonReader.endDocument()
}


@RaptorDsl
public fun MongoBsonReaderScope.fieldName(): String =
	bsonReader.fieldName()


@RaptorDsl
public fun MongoBsonReaderScope.int(): Int =
	bsonReader.int()


@RaptorDsl
public fun MongoBsonReaderScope.long(): Long =
	bsonReader.long()


@RaptorDsl
public fun MongoBsonReaderScope.nextBsonType(): BsonType =
	bsonReader.nextBsonType()


@RaptorDsl
public fun MongoBsonReaderScope.nullValue(): Nothing? =
	bsonReader.nullValue()


@RaptorDsl
public fun MongoBsonReaderScope.objectId(): ObjectId =
	bsonReader.objectId()


@RaptorDsl
public fun MongoBsonReaderScope.skipValue() {
	bsonReader.skipValue()
}


@RaptorDsl
public fun MongoBsonReaderScope.startArray() {
	bsonReader.startArray()
}


@RaptorDsl
public fun MongoBsonReaderScope.startDocument() {
	bsonReader.startDocument()
}


@RaptorDsl
public fun MongoBsonReaderScope.string(): String =
	bsonReader.string()


@RaptorDsl
public fun MongoBsonReaderScope.timestamp(): Timestamp =
	bsonReader.timestamp()


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.array(crossinline read: Scope.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	startArray()

	return read().also { endArray() }
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.array(field: String, crossinline read: Scope.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return array(read)
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope> Scope.arrayByElement(crossinline readElement: Scope.() -> Unit) {
	contract {
		callsInPlace(readElement, InvocationKind.UNKNOWN)
	}

	array {
		while (nextBsonType() != BsonType.END_OF_DOCUMENT)
			readElement()
	}
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.arrayOrNull(crossinline read: Scope.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return array(read)
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.arrayOrNull(field: String, crossinline read: Scope.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return arrayOrNull(read)
}


@RaptorDsl
public fun MongoBsonReaderScope.boolean(field: String): Boolean {
	fieldName(field)

	return boolean()
}


@RaptorDsl
public fun MongoBsonReaderScope.booleanOrNull(): Boolean? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return boolean()
}


@RaptorDsl
public fun MongoBsonReaderScope.booleanOrNull(field: String): Boolean? {
	fieldName(field)

	return booleanOrNull()
}


@RaptorDsl
public fun MongoBsonReaderScope.byteArray(field: String): ByteArray {
	fieldName(field)

	return byteArray()
}


@RaptorDsl
public fun MongoBsonReaderScope.byteArrayOrNull(): ByteArray? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return byteArray()
}


@RaptorDsl
public fun MongoBsonReaderScope.byteArrayOrNull(field: String): ByteArray? {
	fieldName(field)

	return byteArrayOrNull()
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.document(crossinline read: Scope.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	startDocument()

	return read().also { endDocument() }
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.document(field: String, crossinline read: Scope.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return document(read)
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope> Scope.documentByField(crossinline readField: Scope.(field: String) -> Unit) {
	contract {
		callsInPlace(readField, InvocationKind.UNKNOWN)
	}

	document {
		while (nextBsonType() != BsonType.END_OF_DOCUMENT)
			readField(fieldName())
	}
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope> Scope.documentByField(field: String, crossinline readField: Scope.(field: String) -> Unit) {
	contract {
		callsInPlace(readField, InvocationKind.UNKNOWN)
	}

	fieldName(field)

	documentByField(readField)
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.documentOrNull(crossinline read: Scope.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	startDocument()

	return read().also { endDocument() }
}


@RaptorDsl
public inline fun <Scope : MongoBsonReaderScope, Value> Scope.documentOrNull(field: String, crossinline read: Scope.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return documentOrNull(read)
}


@RaptorDsl
public fun MongoBsonReaderScope.double(field: String): Double {
	fieldName(field)

	return double()
}


@RaptorDsl
public fun MongoBsonReaderScope.doubleOrNull(): Double? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return double()
}


@RaptorDsl
public fun MongoBsonReaderScope.doubleOrNull(field: String): Double? {
	fieldName(field)

	return doubleOrNull()
}


@RaptorDsl
public fun MongoBsonReaderScope.fieldName(name: String) {
	val actualName = fieldName()

	check(actualName == name) { "Expected field name '$name' but found '$actualName'." }
}


@RaptorDsl
public fun MongoBsonReaderScope.int(field: String): Int {
	fieldName(field)

	return int()
}


@RaptorDsl
public fun MongoBsonReaderScope.intOrNull(): Int? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return int()
}


@RaptorDsl
public fun MongoBsonReaderScope.intOrNull(field: String): Int? {
	fieldName(field)

	return intOrNull()
}


@RaptorDsl
public fun MongoBsonReaderScope.long(field: String): Long {
	fieldName(field)

	return long()
}


@RaptorDsl
public fun MongoBsonReaderScope.longOrNull(): Long? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return long()
}


@RaptorDsl
public fun MongoBsonReaderScope.longOrNull(field: String): Long? {
	fieldName(field)

	return longOrNull()
}


@RaptorDsl
public fun MongoBsonReaderScope.objectId(field: String): ObjectId {
	fieldName(field)

	return objectId()
}


@RaptorDsl
public fun MongoBsonReaderScope.objectIdOrNull(): ObjectId? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return objectId()
}


@RaptorDsl
public fun MongoBsonReaderScope.objectIdOrNull(field: String): ObjectId? {
	fieldName(field)

	return objectIdOrNull()
}


@RaptorDsl
public fun MongoBsonReaderScope.startArray(field: String) {
	fieldName(field)
	startArray()
}


@RaptorDsl
public fun MongoBsonReaderScope.startDocument(field: String) {
	fieldName(field)
	startDocument()
}


@RaptorDsl
public fun MongoBsonReaderScope.string(field: String): String {
	fieldName(field)

	return string()
}


@RaptorDsl
public fun MongoBsonReaderScope.stringOrNull(): String? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return string()
}


@RaptorDsl
public fun MongoBsonReaderScope.stringOrNull(field: String): String? {
	fieldName(field)

	return stringOrNull()
}


@RaptorDsl
public fun MongoBsonReaderScope.timestamp(field: String): Timestamp {
	fieldName(field)

	return timestamp()
}


@RaptorDsl
public fun MongoBsonReaderScope.timestampOrNull(): Timestamp? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return timestamp()
}


@RaptorDsl
public fun MongoBsonReaderScope.timestampOrNull(field: String): Timestamp? {
	fieldName(field)

	return timestampOrNull()
}
