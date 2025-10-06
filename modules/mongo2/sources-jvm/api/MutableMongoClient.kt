package io.fluidsonic.raptor.mongo2


public interface MutableMongoClient : MongoClient {

	public override fun database(name: String): MutableMongoDatabase
	public override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MutableMongoClient


	public companion object;


	public interface Builder : MongoClient.Builder {

		public override fun build(): MutableMongoClient
		public override fun coderRegistry(coderRegistry: MongoCoderRegistry): Builder
		public override fun connectionString(connectionString: String): Builder
	}
}


public fun MutableMongoClient.Companion.default(): MutableMongoClient.Builder =
	DefaultMongoClientBuilder()
