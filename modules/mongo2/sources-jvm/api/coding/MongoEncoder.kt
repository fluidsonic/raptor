package io.fluidsonic.raptor.mongo2

import org.bson.codecs.*


public interface MongoEncoder<in Value : Any> {

	public fun encodes(type: MongoValueType<out Value>): Boolean
	public fun MongoEncoderScope.encode(value: Value, type: MongoValueType<out Value>)


	public companion object
}


public fun <Value : Any> Codec<Value>.asMongoEncoder(): MongoEncoder<Value> =
	MongoEncoderAsLegacyCodecAdapter(this)
