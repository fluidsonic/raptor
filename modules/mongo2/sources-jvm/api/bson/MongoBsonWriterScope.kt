package io.fluidsonic.raptor.mongo2

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlin.contracts.*
import org.bson.types.*


public interface MongoBsonWriterScope {

	@RaptorDsl
	public val bsonWriter: MongoBsonWriter
}


@RaptorDsl
public inline fun <Writer : MongoBsonWriterScope> Writer.array(field: String, crossinline write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)
	array(write)
}


@RaptorDsl
public inline fun <Writer : MongoBsonWriterScope> Writer.array(crossinline write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	startArray()
	write()
	endArray()
}


@RaptorDsl
public fun MongoBsonWriterScope.boolean(value: Boolean) {
	bsonWriter.boolean(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.boolean(field: String, value: Boolean) {
	fieldName(field)
	boolean(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.booleanOrNull(value: Boolean?) {
	when (value) {
		null -> nullValue()
		else -> boolean(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.booleanOrNull(field: String, value: Boolean?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	booleanOrNull(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.byteArray(value: ByteArray) {
	bsonWriter.byteArray(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.byteArray(field: String, value: ByteArray) {
	fieldName(field)
	byteArray(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.byteArrayOrNull(value: ByteArray?) {
	when (value) {
		null -> nullValue()
		else -> byteArray(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.byteArrayOrNull(field: String, value: ByteArray?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	byteArrayOrNull(value)
}


@RaptorDsl
public inline fun <Writer : MongoBsonWriterScope> Writer.document(field: String, crossinline write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)
	document(write)
}


@RaptorDsl
public inline fun <Writer : MongoBsonWriterScope> Writer.document(crossinline write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	startDocument()
	write()
	endDocument()
}


@RaptorDsl
public fun MongoBsonWriterScope.double(value: Double) {
	bsonWriter.double(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.double(field: String, value: Double) {
	fieldName(field)
	double(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.doubleOrNull(value: Double?) {
	when (value) {
		null -> nullValue()
		else -> double(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.doubleOrNull(field: String, value: Double?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	doubleOrNull(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.endArray() {
	bsonWriter.endArray()
}


@RaptorDsl
public fun MongoBsonWriterScope.endDocument() {
	bsonWriter.endDocument()
}


@RaptorDsl
public fun MongoBsonWriterScope.fieldName(name: String) {
	bsonWriter.fieldName(name)
}


@RaptorDsl
public fun MongoBsonWriterScope.int(value: Int) {
	bsonWriter.int(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.int(field: String, value: Int) {
	fieldName(field)
	int(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.intOrNull(value: Int?) {
	when (value) {
		null -> nullValue()
		else -> int(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.intOrNull(field: String, value: Int?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	intOrNull(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.long(value: Long) {
	bsonWriter.long(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.long(field: String, value: Long) {
	fieldName(field)
	long(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.longOrNull(value: Long?) {
	when (value) {
		null -> nullValue()
		else -> long(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.longOrNull(field: String, value: Long?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	longOrNull(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.nullValue() {
	bsonWriter.nullValue()
}


@RaptorDsl
public fun MongoBsonWriterScope.objectId(value: ObjectId) {
	bsonWriter.objectId(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.objectIdOrNull(field: String, value: ObjectId) {
	fieldName(field)
	objectId(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.objectIdOrNull(value: ObjectId?) {
	when (value) {
		null -> nullValue()
		else -> objectId(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.objectIdOrNull(field: String, value: ObjectId?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	objectIdOrNull(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.startArray() {
	bsonWriter.startArray()
}


@RaptorDsl
public fun MongoBsonWriterScope.startDocument() {
	bsonWriter.startDocument()
}


@RaptorDsl
public fun MongoBsonWriterScope.string(value: String) {
	bsonWriter.string(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.string(field: String, value: String) {
	fieldName(field)
	string(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.stringOrNull(value: String?) {
	when (value) {
		null -> nullValue()
		else -> string(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.stringOrNull(field: String, value: String?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	stringOrNull(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.timestamp(value: Timestamp) {
	bsonWriter.timestamp(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.timestamp(field: String, value: Timestamp) {
	fieldName(field)
	timestamp(value)
}


@RaptorDsl
public fun MongoBsonWriterScope.timestampOrNull(value: Timestamp?) {
	when (value) {
		null -> nullValue()
		else -> timestamp(value)
	}
}


@RaptorDsl
public fun MongoBsonWriterScope.timestampOrNull(field: String, value: Timestamp?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	timestampOrNull(value)
}
