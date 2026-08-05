package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlin.contracts.*
import kotlin.reflect.*
import org.bson.*
import org.bson.types.*


public interface RaptorBsonWriter {

	@RaptorDsl
	public fun endArray()

	@RaptorDsl
	public fun endDocument()

	@RaptorDsl
	public fun fieldName(name: String)

	@RaptorInternalApi
	@RaptorDsl
	public fun internal(): BsonWriter

	@RaptorDsl
	public fun startArray()

	@RaptorDsl
	public fun startDocument()

	@RaptorDsl
	public fun value(value: Any?)

	@RaptorDsl
	public fun value(value: Boolean)

	@RaptorDsl
	public fun value(value: Double)

	@RaptorDsl
	public fun value(value: Float)

	@RaptorDsl
	public fun value(value: Int)

	@RaptorDsl
	public fun value(value: Long)

	@RaptorDsl
	public fun value(value: Nothing?)

	@RaptorDsl
	public fun value(value: ObjectId)

	@RaptorDsl
	public fun value(value: Short)

	@RaptorDsl
	public fun value(value: String)

	@RaptorDsl
	public fun value(value: Timestamp)

	@RaptorDsl
	public fun <Value : Any> value(type: RaptorBsonType<Value>, value: Value?)

	/**
	 * Writes [value] as an unboxed `Boolean`, ignoring [type].
	 *
	 * Unlike the generic overload this does not consult the codec registry, so none of the coercions the
	 * registry's `Boolean` codec would apply take place — the value is written directly, the same way the
	 * corresponding read-side overload avoids boxing.
	 */
	@RaptorDsl
	public fun value(type: RaptorBsonType<Boolean>, value: Boolean)

	/**
	 * Writes [value] as an unboxed `Double`, ignoring [type].
	 *
	 * Unlike the generic overload this does not consult the codec registry, so none of the numeric widening
	 * the registry's `Double` codec would apply takes place — the value is written directly, the same way
	 * the corresponding read-side overload avoids boxing.
	 */
	@RaptorDsl
	public fun value(type: RaptorBsonType<Double>, value: Double)

	/**
	 * Writes [value] as an unboxed `Int`, ignoring [type].
	 *
	 * Unlike the generic overload this does not consult the codec registry, so none of the numeric narrowing
	 * the registry's `Int` codec would apply takes place — the value is written directly, the same way the
	 * corresponding read-side overload avoids boxing.
	 */
	@RaptorDsl
	public fun value(type: RaptorBsonType<Int>, value: Int)

	/**
	 * Writes [value] as an unboxed `Long`, ignoring [type].
	 *
	 * Unlike the generic overload this does not consult the codec registry, so none of the coercions the
	 * registry's `Long` codec would apply take place — the value is written directly, the same way the
	 * corresponding read-side overload avoids boxing.
	 */
	@RaptorDsl
	public fun value(type: RaptorBsonType<Long>, value: Long)

	@RaptorDsl
	public fun <Value : Any> valueAs(value: Value?, valueClass: KClass<out Value>)
}


@RaptorDsl
public inline fun <Writer : RaptorBsonWriter> Writer.array(field: String, write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)
	array(write)
}


@RaptorDsl
public inline fun <Writer : RaptorBsonWriter> Writer.array(write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	startArray()
	write()
	endArray()
}


@RaptorDsl
public inline fun <Writer : RaptorBsonWriter> Writer.document(field: String, write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	fieldName(field)
	document(write)
}


@RaptorDsl
public inline fun <Writer : RaptorBsonWriter> Writer.document(write: Writer.() -> Unit) {
	contract {
		callsInPlace(write, InvocationKind.EXACTLY_ONCE)
	}

	startDocument()
	write()
	endDocument()
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Any?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Boolean) {
	fieldName(field)
	value(value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Boolean?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Double) {
	fieldName(field)
	value(value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Double?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Float) {
	fieldName(field)
	value(value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Float?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Int) {
	fieldName(field)
	value(value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Int?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Long) {
	fieldName(field)
	value(value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Long?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: ObjectId?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Short) {
	fieldName(field)
	value(value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Short?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: String?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, value: Timestamp?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> value(value)
	}
}


@RaptorDsl
public fun <Value : Any> RaptorBsonWriter.value(field: String, type: RaptorBsonType<Value>, value: Value?, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	value(type, value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, type: RaptorBsonType<Boolean>, value: Boolean) {
	fieldName(field)
	value(type, value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, type: RaptorBsonType<Double>, value: Double) {
	fieldName(field)
	value(type, value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, type: RaptorBsonType<Int>, value: Int) {
	fieldName(field)
	value(type, value)
}


@RaptorDsl
public fun RaptorBsonWriter.value(field: String, type: RaptorBsonType<Long>, value: Long) {
	fieldName(field)
	value(type, value)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorBsonWriter.valueAs(value: Value?) {
	valueAs(value = value, valueClass = Value::class)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorBsonWriter.valueAs(
	field: String,
	value: Value?,
	preserveNull: Boolean = false,
	valueClass: KClass<out Value>,
) {
	if (value == null && !preserveNull)
		return

	fieldName(field)

	when (value) {
		null -> value(null)
		else -> valueAs(value, valueClass = valueClass)
	}
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorBsonWriter.valueAs(field: String, value: Value?, preserveNull: Boolean = false) {
	valueAs(field = field, value = value, preserveNull = preserveNull, valueClass = Value::class)
}
