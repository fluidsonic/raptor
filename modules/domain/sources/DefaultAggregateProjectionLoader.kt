package io.fluidsonic.raptor.cqrs

import java.util.concurrent.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow


// FIXME How to receive events?
// FIXME rename class
internal class DefaultAggregateProjectionLoader<
	out Projection : RaptorAggregateProjection<Id>,
	Id : RaptorAggregateProjectionId,
	in Event : RaptorAggregateEvent<Id>,
	>(
	private val factory: () -> RaptorAggregateProjector.Incremental<Projection, Id, Event>,
) : RaptorAggregateProjectionLoader<Projection, Id> {

	private val projectors = ConcurrentHashMap<Id, RaptorAggregateProjector.Incremental<Projection, Id, Event>>()


	internal fun addEvent(event: RaptorEvent<Id, Event>) {
		projectors.getOrPut(event.aggregateId, factory).add(event)
	}


	override fun loadAll(): Flow<Projection> =
		flow {
			withContext(Dispatchers.Default) { // TODO Ok?
				projectors.values.mapNotNull { it.projection }.forEach { emit(it) } // TODO Probably not concurrency-safe.
			}
		}


	override suspend fun loadOrNull(id: Id): Projection? =
		projectors[id]?.projection
}
