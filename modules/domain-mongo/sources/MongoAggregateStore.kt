package io.fluidsonic.raptor.domain.mongo

import com.mongodb.client.model.*
import com.mongodb.client.model.Sorts.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.domain.mongo.RaptorAggregateEventBson.Fields
import kotlinx.coroutines.flow.*


// TODO Support horizontal scaling.
private class MongoAggregateStore(
	private val collection: MongoCollection<RaptorAggregateEvent<*, *>>,
) : RaptorAggregateStore {

	override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
		// TODO Batch in case the number of events is large.
		// FIXME tx
		collection.insertMany(events)
	}


	// TODO Can still lead to different order than written. We don't have enough data to maintain insertion order.
	override fun load(): Flow<RaptorAggregateEvent<*, *>> =
		collection.find().sort(orderBy(
			ascending(Fields.timestamp),
			ascending(Fields.version),
		))


	override suspend fun start() {
		collection.createIndex(Indexes.ascending(Fields.timestamp))
	}
}


public fun RaptorAggregateStore.Companion.mongo(collection: MongoCollection<RaptorAggregateEvent<*, *>>): RaptorAggregateStore =
	MongoAggregateStore(collection = collection)
