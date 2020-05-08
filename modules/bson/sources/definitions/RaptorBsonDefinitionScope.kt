package io.fluidsonic.raptor

import org.bson.*


interface RaptorBsonDefinitionScope<Value : Any> : BsonScope {

	fun decode(decoder: (BsonReader.() -> Value))

	fun encode(encoder: BsonWriter.(value: Value) -> Unit)
}
