package io.fluidsonic.raptor.mongo2


internal object EmptyMongoDecoderRegistry : MongoDecoderRegistry {

	override fun <Value : Any> findOrNull(type: MongoValueType<Value>): MongoDecoder<Value>? =
		null
}
