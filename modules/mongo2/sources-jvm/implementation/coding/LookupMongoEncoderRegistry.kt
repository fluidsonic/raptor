package io.fluidsonic.raptor.mongo2

import java.util.concurrent.*


internal data class LookupMongoEncoderRegistry(
	private val lookup: (valueType: MongoValueType<*>) -> MongoEncoder<*>?,
) : MongoEncoderRegistry {

	private val cache = ConcurrentHashMap<MongoValueType<*>, MongoEncoder<*>>()


	override fun <Value : Any> findOrNull(type: MongoValueType<Value>): MongoEncoder<Value>? =
		cache.getOrPut(type) {
			lookup(type) ?: NotFoundEncoder
		}.takeUnless { it === NotFoundEncoder }
			as MongoEncoder<Value>? // FIXME


	private object NotFoundEncoder : MongoEncoder<Nothing> {

		override fun encodes(type: MongoValueType<out Nothing>) =
			false


		override fun MongoEncoderScope.encode(value: Nothing, type: MongoValueType<out Nothing>) =
			error("This is just a marker that no encoder was found.")
	}
}
