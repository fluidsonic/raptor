package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.cqrs.*
import kotlinx.coroutines.flow.*


// TODO Support horizontal scaling.
public class RaptorMongoAggregateStore(
	private val collection: MongoCollection<RaptorEvent<*, *>>,
) : RaptorAggregateStore {

	override suspend fun add(events: List<RaptorEvent<*, *>>) {
		// TODO Batch in case the number of events is large.
		// FIXME tx
		collection.insertMany(events)
	}


	override fun load(): Flow<RaptorEvent<*, *>> =
		collection.find() // FIXME Needs explicit order. Capped collections won't work as they are size-limited & don't support transactions.
}
