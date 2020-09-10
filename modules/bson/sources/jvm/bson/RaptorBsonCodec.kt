package io.fluidsonic.raptor

import kotlin.reflect.*


public interface RaptorBsonCodec<Value : Any> {

	public val valueClass: KClass<Value>

	public fun RaptorBsonReaderScope.decode(): Value

	public fun RaptorBsonWriterScope.encode(value: Value)
}


public fun <Value : Any> RaptorBsonCodec<Value>.decode(scope: RaptorBsonReaderScope): Value =
	with(scope) {
		decode()
	}


public fun <Value : Any> RaptorBsonCodec<Value>.encode(scope: RaptorBsonWriterScope, value: Value) {
	with(scope) {
		encode(value)
	}
}
