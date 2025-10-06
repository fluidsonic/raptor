package io.fluidsonic.raptor.mongo2


internal data class ImmutableMongoClient(
	private val source: MongoClient,
) : MongoClient by source {

	override fun asImmutable(): MongoClient =
		this


	override fun database(name: String): MongoDatabase =
		source.database(name).asImmutable()


	override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MongoClient =
		source.withCoderRegistry(coderRegistry).asImmutable()
}
