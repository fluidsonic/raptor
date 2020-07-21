package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*
import kotlin.reflect.*


interface RaptorBsonDefinition<Value : Any> {

	val valueClass: KClass<Value>

	fun codec(scope: BsonScope): Codec<Value>? =
		null

	fun provider(scope: BsonScope): CodecProvider? =
		null

	fun registry(scope: BsonScope): CodecRegistry? =
		null
}
