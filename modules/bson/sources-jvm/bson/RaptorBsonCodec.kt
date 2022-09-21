package io.fluidsonic.raptor.bson

import kotlin.reflect.*


public interface RaptorBsonCodec<Value : Any> {

	public val valueClass: KClass<Value>

	public fun RaptorBsonReaderScope.decode(arguments: List<KTypeProjection>?): Value

	public fun RaptorBsonWriterScope.encode(value: Value)
}


public fun <Value : Any> RaptorBsonCodec<Value>.decode(scope: RaptorBsonReaderScope, arguments: List<KTypeProjection>): Value =
	with(scope) {
		decode(arguments = arguments)
	}


public fun <Value : Any> RaptorBsonCodec<Value>.encode(scope: RaptorBsonWriterScope, value: Value) {
	with(scope) {
		encode(value)
	}
}
