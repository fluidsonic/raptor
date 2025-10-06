package io.fluidsonic.raptor.mongo2

import com.mongodb.reactivestreams.client.MongoClient


internal data class DefaultMongoClient(
	override val coderRegistry: MongoCoderRegistry,
	private val source: MongoClient,
) : MutableMongoClient {

	override fun database(name: String): MutableMongoDatabase =
		DefaultMongoDatabase(
			coderRegistry = coderRegistry,
			source = source.getDatabase(name),
		)


	override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MutableMongoClient =
		copy(coderRegistry = coderRegistry)
}
