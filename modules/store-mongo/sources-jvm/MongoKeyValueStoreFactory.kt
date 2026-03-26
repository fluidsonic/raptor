package io.fluidsonic.raptor.store.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.store.*
import kotlin.reflect.*


private class MongoKeyValueStoreFactory(
	private val database: MongoDatabase,
) : RaptorKeyValueStoreFactory {

	override fun <Key : Any, Value : Any> create(name: String, keyClass: KClass<Key>, valueClass: KClass<Value>): RaptorKeyValueStore<Key, Value> =
		MongoKeyValueStore(
			collection = database.getCollectionOf(name),
			keyClass = keyClass,
			valueClass = valueClass,
		)
}


public fun RaptorKeyValueStoreFactory.Companion.mongo(database: MongoDatabase): RaptorKeyValueStoreFactory =
	MongoKeyValueStoreFactory(database = database)
