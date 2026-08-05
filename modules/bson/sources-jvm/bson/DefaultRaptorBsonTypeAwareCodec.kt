package io.fluidsonic.raptor.bson

import kotlin.reflect.*


// The codec a `raptor.bson.definition { … }` produces once it carries a `decodeWithType { … }` block. It
// inherits the base `decode`/`encode` behavior unchanged — the added interface only ever takes over when the
// value is read through a `RaptorBsonType`.
internal class DefaultRaptorBsonTypeAwareCodec<Value : Any>(
	decode: (RaptorBsonReaderScope.(arguments: List<KTypeProjection>?) -> Value)?,
	private val decodeWithType: RaptorBsonReaderScope.(type: RaptorBsonType<*>) -> Value,
	encode: (RaptorBsonWriterScope.(value: Value) -> Unit)?,
	valueClass: KClass<Value>,
) : DefaultRaptorBsonCodec<Value>(decode = decode, encode = encode, valueClass = valueClass), RaptorBsonTypeAwareCodec<Value> {

	override fun RaptorBsonReaderScope.decode(type: RaptorBsonType<*>): Value =
		decodeWithType.invoke(this, type)


	override fun toString() =
		"Raptor BSON codec (${valueClass.qualifiedName}, type-aware)"
}
