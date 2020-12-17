package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlin.contracts.*
import kotlin.reflect.*
import org.bson.*
import org.bson.types.*


public interface RaptorBsonReader {

	@RaptorDsl
	public fun boolean(): Boolean

	@RaptorDsl
	public fun bsonType(): BsonType

	@RaptorDsl
	public fun double(): Double

	@RaptorDsl
	public fun endArray()

	@RaptorDsl
	public fun endDocument()

	@RaptorDsl
	public fun fieldName(): String

	@RaptorDsl
	public fun int(): Int

	@InternalRaptorApi
	@RaptorDsl
	public fun internal(): BsonReader

	@RaptorDsl
	public fun long(): Long

	@RaptorDsl
	public fun nextBsonType(): BsonType

	@RaptorDsl
	public fun objectId(): ObjectId

	@RaptorDsl
	public fun skipValue()

	@RaptorDsl
	public fun startArray()

	@RaptorDsl
	public fun startDocument()

	@RaptorDsl
	public fun string(): String

	@RaptorDsl
	public fun timestamp(): Timestamp

	@RaptorDsl
	public fun <Value> value(type: KType): Value
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.array(read: Reader.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	startArray()

	return read().also { endArray() }
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.array(field: String, read: Reader.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return array(read)
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader> Reader.arrayByElement(readElement: Reader.() -> Unit) {
	contract {
		callsInPlace(readElement, InvocationKind.UNKNOWN)
	}

	array {
		while (nextBsonType() != BsonType.END_OF_DOCUMENT)
			readElement()
	}
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.arrayOrNull(read: Reader.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return array(read)
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.arrayOrNull(field: String, read: Reader.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return arrayOrNull(read)
}


@RaptorDsl
public fun RaptorBsonReader.boolean(field: String): Boolean {
	fieldName(field)

	return boolean()
}


@RaptorDsl
public fun RaptorBsonReader.booleanOrNull(): Boolean? {
	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return boolean()
}


@RaptorDsl
public fun RaptorBsonReader.booleanOrNull(field: String): Boolean? {
	fieldName(field)

	return booleanOrNull()
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.document(read: Reader.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	startDocument()

	return read().also { endDocument() }
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.document(field: String, read: Reader.() -> Value): Value {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return document(read)
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader> Reader.documentByField(readField: Reader.(field: String) -> Unit) {
	contract {
		callsInPlace(readField, InvocationKind.UNKNOWN)
	}

	document {
		while (nextBsonType() != BsonType.END_OF_DOCUMENT)
			readField(fieldName())
	}
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.documentOrNull(read: Reader.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	startDocument()

	return read().also { endDocument() }
}


@RaptorDsl
public inline fun <Reader : RaptorBsonReader, Value> Reader.documentOrNull(field: String, read: Reader.() -> Value): Value? {
	contract {
		callsInPlace(read, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)

	return documentOrNull(read)
}


@RaptorDsl
public fun RaptorBsonReader.double(field: String): Double {
	fieldName(field)

	return double()
}


@RaptorDsl
public fun RaptorBsonReader.doubleOrNull(): Double? {
	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return double()
}


@RaptorDsl
public fun RaptorBsonReader.doubleOrNull(field: String): Double? {
	fieldName(field)

	return doubleOrNull()
}


@RaptorDsl
public fun RaptorBsonReader.fieldName(name: String) {
	val actualName = fieldName()

	check(actualName == name) { "Expected field name '$name' but found '$actualName'." }
}


@RaptorDsl
public fun RaptorBsonReader.int(field: String): Int {
	fieldName(field)

	return int()
}


@RaptorDsl
public fun RaptorBsonReader.intOrNull(): Int? {
	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return int()
}


@RaptorDsl
public fun RaptorBsonReader.intOrNull(field: String): Int? {
	fieldName(field)

	return intOrNull()
}


@RaptorDsl
public fun RaptorBsonReader.long(field: String): Long {
	fieldName(field)

	return long()
}


@RaptorDsl
public fun RaptorBsonReader.longOrNull(): Long? {
	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return long()
}


@RaptorDsl
public fun RaptorBsonReader.longOrNull(field: String): Long? {
	fieldName(field)

	return longOrNull()
}


@RaptorDsl
public fun RaptorBsonReader.objectId(field: String): ObjectId {
	fieldName(field)

	return objectId()
}


@RaptorDsl
public fun RaptorBsonReader.objectIdOrNull(): ObjectId? {
	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return objectId()
}


@RaptorDsl
public fun RaptorBsonReader.objectIdOrNull(field: String): ObjectId? {
	fieldName(field)

	return objectIdOrNull()
}


@RaptorDsl
public fun RaptorBsonReader.startArray(field: String) {
	fieldName(field)
	startArray()
}


@RaptorDsl
public fun RaptorBsonReader.startDocument(field: String) {
	fieldName(field)
	startDocument()
}


@RaptorDsl
public fun RaptorBsonReader.string(field: String): String {
	fieldName(field)

	return string()
}


@RaptorDsl
public fun RaptorBsonReader.stringOrNull(): String? {
	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return string()
}


@RaptorDsl
public fun RaptorBsonReader.stringOrNull(field: String): String? {
	fieldName(field)

	return stringOrNull()
}


@RaptorDsl
public fun RaptorBsonReader.timestamp(field: String): Timestamp {
	fieldName(field)

	return timestamp()
}


@RaptorDsl
public fun RaptorBsonReader.timestampOrNull(): Timestamp? {
	if (bsonType() == BsonType.NULL) // FIXME probably wrong
		return null

	return timestamp()
}


@RaptorDsl
public fun RaptorBsonReader.timestampOrNull(field: String): Timestamp? {
	fieldName(field)

	return timestampOrNull()
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Value> RaptorBsonReader.value(): Value {
	if (bsonType() == BsonType.NULL && typeOf<Value>().isMarkedNullable)
		return null as Value

	return value(typeOf<Value>())
}


@RaptorDsl
public inline fun <reified Value> RaptorBsonReader.value(field: String): Value {
	fieldName(field)

	return value()
}
