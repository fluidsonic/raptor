package io.fluidsonic.raptor.mongo2

import io.fluidsonic.raptor.*


public interface MongoEncoderScope : MongoBsonWriterScope {

	@RaptorDsl
	public val context: MongoEncoderContext


	public companion object
}


@RaptorDsl
public fun <Value : Any> MongoEncoderScope.value(value: Value, type: MongoValueType<Value>) {
	with(context.encoderRegistry.find(type)) {
		encode(value, type)
	}
}


@RaptorDsl
public fun <Value : Any> MongoEncoderScope.value(field: String, value: Value, type: MongoValueType<Value>) {
	fieldName(field)
	value(value, type)
}


@RaptorDsl
public fun <Value : Any> MongoEncoderScope.valueOrNull(value: Value?, type: MongoValueType<Value>) {
	when (value) {
		null -> nullValue()
		else -> value(value, type)
	}
}


@RaptorDsl
public fun <Value : Any> MongoEncoderScope.valueOrNull(field: String, value: Value?, type: MongoValueType<Value>, preserveNull: Boolean = false) {
	if (value == null && !preserveNull)
		return

	fieldName(field)
	valueOrNull(value, type)
}
