package io.fluidsonic.raptor.store.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.store.*


internal class MongoLogStore<Value : Any>(
	private val collection: MongoCollection<Value>,
) : RaptorLogStore<Value> {

	override suspend fun append(value: Value) {
		collection.insertOne(value)
	}
}
