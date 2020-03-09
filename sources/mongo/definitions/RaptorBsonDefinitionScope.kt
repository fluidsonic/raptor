package io.fluidsonic.raptor

import org.bson.*


interface RaptorBsonDefinitionScope<Value : Any> : RaptorBsonScope {

	fun decode(decoder: (BsonReader.() -> Value))

	fun encode(encoder: BsonWriter.(value: Value) -> Unit)
}
