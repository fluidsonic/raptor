package io.fluidsonic.raptor.mongo2


internal data class ImmutableMongoDatabase(
	private val source: MongoDatabase,
) : MongoDatabase by source {

	override fun asImmutable(): MongoDatabase =
		this


	override fun <Value : Any> collection(name: String, valueType: MongoValueType<Value>): MongoCollection<Value> =
		source.collection(name, valueType).asImmutable()


	override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MongoDatabase =
		source.withCoderRegistry(coderRegistry).asImmutable()
}
