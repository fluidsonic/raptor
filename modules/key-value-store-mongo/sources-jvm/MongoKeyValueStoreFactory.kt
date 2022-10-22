package io.fluidsonic.raptor.keyvaluestore.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.keyvaluestore.*
import kotlin.reflect.*


private class MongoKeyValueStoreFactory(
	private val database: MongoDatabase,
) : KeyValueStoreFactory {

	override fun <Key : Any, Value : Any> create(name: String, keyClass: KClass<Key>, valueClass: KClass<Value>): KeyValueStore<Key, Value> =
		MongoKeyValueStore(
			database = database,
			collectionName = name,
			keyClass = keyClass,
			valueClass = valueClass,
		)
}


public fun KeyValueStoreFactory.Companion.mongo(database: MongoDatabase): KeyValueStoreFactory =
	MongoKeyValueStoreFactory(database = database)
