package io.fluidsonic.raptor

import org.bson.codecs.*


interface RaptorBsonDefinition<Value : Any> {

	fun codec(scope: BsonScope): Codec<Value>
}
