package io.fluidsonic.raptor.mongo2


public interface MongoClient {

	public val coderRegistry: MongoCoderRegistry

	public fun database(name: String): MongoDatabase
	public fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MongoClient


	public fun asImmutable(): MongoClient =
		ImmutableMongoClient(this)


	public companion object;


	public interface Builder {

		public fun build(): MongoClient
		public fun coderRegistry(coderRegistry: MongoCoderRegistry): Builder
		public fun connectionString(connectionString: String): Builder
	}
}


public fun MongoClient.Companion.default(): MongoClient.Builder =
	DefaultMongoClientBuilder().asImmutable()
