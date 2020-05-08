package io.fluidsonic.raptor

import org.bson.codecs.*
import kotlin.reflect.*


interface RaptorBsonDefinition<Value : Any> {

	val valueClass: KClass<Value>

	fun codec(scope: BsonScope): Codec<Value>
}
