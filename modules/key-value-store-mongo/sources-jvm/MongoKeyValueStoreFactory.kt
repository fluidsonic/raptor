package io.fluidsonic.raptor.keyvaluestore.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.keyvaluestore.*
import kotlin.reflect.*


private class MongoKeyValueStoreFactory(
	private val database: MongoDatabase,
) : RaptorKeyValueStoreFactory {

	override fun <Key : Any, Value : Any> create(name: String, keyClass: KClass<Key>, valueClass: KClass<Value>): RaptorKeyValueStore<Key, Value> =
		MongoKeyValueStore(
			database = database,
			collectionName = name,
			keyClass = keyClass,
			valueClass = valueClass,
		)
}


public fun RaptorKeyValueStoreFactory.Companion.mongo(database: MongoDatabase): RaptorKeyValueStoreFactory =
	MongoKeyValueStoreFactory(database = database)
