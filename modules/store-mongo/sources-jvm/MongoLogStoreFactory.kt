package io.fluidsonic.raptor.store.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.store.*
import kotlin.reflect.*


private class MongoLogStoreFactory(
	private val database: MongoDatabase,
) : RaptorLogStoreFactory {

	override fun <Value : Any> create(name: String, valueClass: KClass<Value>): RaptorLogStore<Value> =
		MongoLogStore(
			collection = database.getCollection(name, documentClass = valueClass),
		)
}


/**
 * Creates a [RaptorLogStoreFactory] backed by MongoDB, using collections in the given [database].
 */
public fun RaptorLogStoreFactory.Companion.mongo(database: MongoDatabase): RaptorLogStoreFactory =
	MongoLogStoreFactory(database = database)
