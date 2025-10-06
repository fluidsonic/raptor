package io.fluidsonic.raptor.mongo2

import org.bson.codecs.*


public interface MongoDecoder<out Value : Any> {

	public fun decodes(type: MongoValueType<in Value>): Boolean
	public fun MongoDecoderScope.decode(type: MongoValueType<in Value>): Value


	public companion object
}


public fun <Value : Any> Codec<Value>.asMongoDecoder(): MongoDecoder<Value> =
	LegacyCodecAsMongoDecoderAdapter(this)
