package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.cqrs.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*


// TODO Support horizontal scaling.
public class MongoAggregateStore(
	private val clock: Clock,
	private val collection: MongoCollection<RaptorEvent<*, *>>,
) : RaptorAggregateStore {

	override suspend fun add(events: List<RaptorEvent<*, *>>) {
		// TODO Batch in case the number of events is large.
		collection.insertMany(events)
	}


	override fun load(): Flow<RaptorEvent<*, *>> =
		collection.find().sort()
}
