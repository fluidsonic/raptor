package io.fluidsonic.raptor.mongo2


public interface MutableMongoDatabase : MongoDatabase {

	public override fun <Value : Any> collection(name: String, valueType: MongoValueType<Value>): MutableMongoCollection<Value>
	public override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MutableMongoDatabase


	public companion object
}


public inline fun <reified Value : Any> MutableMongoDatabase.collection(name: String): MutableMongoCollection<Value> =
	collection(name = name, valueType = MongoValueType<Value>())
