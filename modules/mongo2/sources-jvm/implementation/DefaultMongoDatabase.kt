package io.fluidsonic.raptor.mongo2

import com.mongodb.reactivestreams.client.MongoDatabase as SourceDatabase


internal data class DefaultMongoDatabase(
	override val coderRegistry: MongoCoderRegistry,
	val source: SourceDatabase,
) : MutableMongoDatabase {

	override fun <Value : Any> collection(name: String, valueType: MongoValueType<Value>): MutableMongoCollection<Value> =
		DefaultMongoCollection(
			coderRegistry = coderRegistry,
			database = this,
			name = name,
			valueType = valueType,
		)


	override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MutableMongoDatabase =
		copy(coderRegistry = coderRegistry)
}
