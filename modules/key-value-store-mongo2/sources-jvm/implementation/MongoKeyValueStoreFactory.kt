package io.fluidsonic.raptor.keyvaluestore.mongo2

import io.fluidsonic.raptor.keyvaluestore.*
import io.fluidsonic.raptor.mongo2.*
import kotlin.reflect.*


internal class MongoKeyValueStoreFactory(
	private val database: MutableMongoDatabase,
) : RaptorKeyValueStoreFactory {

	override fun <Key : Any, Value : Any> create(name: String, keyType: KType, valueType: KType): RaptorKeyValueStore<Key, Value> =
		MongoKeyValueStore(
			database = database,
			collectionName = name,
			keyType = MongoValueType(keyType) as MongoValueType<Key>,
			valueType = MongoValueType(valueType) as MongoValueType<Value>,
		)
}
