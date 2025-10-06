package io.fluidsonic.raptor.mongo2


private class ImmutableMongoClientBuilder(
	private val mutable: MutableMongoClient.Builder,
) : MongoClient.Builder {

	override fun build(): MongoClient =
		mutable.build().asImmutable()


	override fun coderRegistry(coderRegistry: MongoCoderRegistry) = also {
		mutable.coderRegistry(coderRegistry)
	}


	override fun connectionString(connectionString: String) = also {
		mutable.connectionString(connectionString)
	}
}


// Not part of public API. No idea yet if it's worth it.
internal fun MutableMongoClient.Builder.asImmutable(): MongoClient.Builder =
	ImmutableMongoClientBuilder(this)
