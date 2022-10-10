package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import kotlinx.coroutines.flow.*


// TODO Support horizontal scaling.
public class RaptorMongoAggregateStore(
	private val collection: MongoCollection<RaptorAggregateEvent<*, *>>,
) : RaptorAggregateStore {

	override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
		// TODO Batch in case the number of events is large.
		// FIXME tx
		collection.insertMany(events)
	}


	override fun load(): Flow<RaptorAggregateEvent<*, *>> =
		collection.find() // FIXME Needs explicit order. Capped collections won't work as they are size-limited & don't support transactions.
}
