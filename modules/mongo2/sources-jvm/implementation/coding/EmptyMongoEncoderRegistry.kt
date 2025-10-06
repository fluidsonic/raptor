package io.fluidsonic.raptor.mongo2


internal object EmptyMongoEncoderRegistry : MongoEncoderRegistry {

	override fun <Value : Any> findOrNull(type: MongoValueType<Value>): MongoEncoder<Value>? =
		null
}
