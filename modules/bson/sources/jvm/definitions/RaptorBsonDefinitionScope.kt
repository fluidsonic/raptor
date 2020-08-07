package io.fluidsonic.raptor

import org.bson.*


@RaptorDsl
public interface RaptorBsonDefinitionScope<Value : Any> : BsonScope {

	@RaptorDsl
	public fun decode(decoder: (BsonReader.() -> Value))

	@RaptorDsl
	public fun encode(encoder: BsonWriter.(value: Value) -> Unit)
}
