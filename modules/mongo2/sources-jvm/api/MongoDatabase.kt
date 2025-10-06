package io.fluidsonic.raptor.mongo2


public interface MongoDatabase {

	public val coderRegistry: MongoCoderRegistry

	public fun <Value : Any> collection(name: String, valueType: MongoValueType<Value>): MongoCollection<Value>
	public fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MongoDatabase


	public fun asImmutable(): MongoDatabase =
		ImmutableMongoDatabase(this)


	public companion object
}


public inline fun <reified Value : Any> MongoDatabase.collection(name: String): MongoCollection<Value> =
	collection(name = name, valueType = MongoValueType<Value>())
