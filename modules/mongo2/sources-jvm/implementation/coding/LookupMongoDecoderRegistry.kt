package io.fluidsonic.raptor.mongo2

import java.util.concurrent.*


internal data class LookupMongoDecoderRegistry(
	private val lookup: (type: MongoValueType<*>) -> MongoDecoder<*>?,
) : MongoDecoderRegistry {

	private val cache = ConcurrentHashMap<MongoValueType<*>, MongoDecoder<*>>()


	override fun <Value : Any> findOrNull(type: MongoValueType<Value>): MongoDecoder<Value>? =
		cache.getOrPut(type) {
			lookup(type) ?: NotFoundDecoder
		}.takeUnless { it === NotFoundDecoder }
			as MongoDecoder<Value>?


	private object NotFoundDecoder : MongoDecoder<Nothing> {

		override fun decodes(type: MongoValueType<in Nothing>) =
			false


		override fun MongoDecoderScope.decode(type: MongoValueType<in Nothing>) =
			error("This is just a marker that no decoder was found.")
	}
}
