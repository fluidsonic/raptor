package io.fluidsonic.raptor

import org.bson.*


@RaptorDsl
interface RaptorBsonDefinitionScope<Value : Any> : BsonScope {

	@RaptorDsl
	fun decode(decoder: (BsonReader.() -> Value))

	@RaptorDsl
	fun encode(encoder: BsonWriter.(value: Value) -> Unit)
}
