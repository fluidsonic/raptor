package io.fluidsonic.raptor.mongo2

import io.fluidsonic.raptor.*
import kotlin.reflect.*
import org.bson.*


public interface MongoDecoderScope : MongoBsonReaderScope {

	@RaptorDsl
	public val context: MongoDecoderContext


	public companion object
}


@RaptorDsl
public inline fun <reified Value> MongoDecoderScope.value(): Value {
	return when (typeOf<Value>().isMarkedNullable) {
		true -> valueOrNull(MongoValueType<Value>()) as Value
		false -> value(MongoValueType<Value>())
	}
}


@RaptorDsl
public fun <Value : Any> MongoDecoderScope.value(type: MongoValueType<Value>): Value =
	// FIXME Simplify.
	with(context.decoderRegistry.find(type)) {
		decode(type)
	}


@RaptorDsl
public inline fun <reified Value> MongoDecoderScope.value(field: String): Value {
	fieldName(field)

	return value()
}


@RaptorDsl
public fun <Value : Any> MongoDecoderScope.value(field: String, type: MongoValueType<Value>): Value {
	fieldName(field)

	return value(type)
}


@RaptorDsl
public inline fun <reified Value : Any> MongoDecoderScope.valueOrNull(): Value? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return value()
}


@RaptorDsl
public fun <Value : Any> MongoDecoderScope.valueOrNull(type: MongoValueType<Value>): Value? {
	if (bsonType() == BsonType.NULL) {
		skipValue()

		return null
	}

	return value(type)
}


@RaptorDsl
public inline fun <reified Value : Any> MongoDecoderScope.valueOrNull(field: String): Value? {
	fieldName(field)

	return valueOrNull()
}


@RaptorDsl
public fun <Value : Any> MongoDecoderScope.valueOrNull(field: String, type: MongoValueType<Value>): Value? {
	fieldName(field)

	return valueOrNull(type)
}
