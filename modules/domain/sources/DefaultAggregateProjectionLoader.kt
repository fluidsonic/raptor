package io.fluidsonic.raptor.domain

import java.util.concurrent.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.Flow


// FIXME How to receive events?
// FIXME rename class
internal class DefaultAggregateProjectionLoader<
	Projection : RaptorAggregateProjection<Id>,
	Id : RaptorAggregateProjectionId,
	Change : RaptorAggregateChange<Id>,
	>(
	private val factory: () -> RaptorAggregateProjector.Incremental<Projection, Id, Change>,
) : RaptorAggregateProjectionLoader<Projection, Id> {

	private val projectors = ConcurrentHashMap<Id, RaptorAggregateProjector.Incremental<Projection, Id, Change>>()


	internal fun addEvent(event: RaptorAggregateEvent<Id, Change>): RaptorAggregateProjectionEvent<Id, Projection, Change> {
		val projector = projectors.getOrPut(event.aggregateId, factory)
		val previousProjection = projector.projection
		val projection = projector.add(event)

		return RaptorAggregateProjectionEvent(
			change = event.change,
			id = event.id,
			previousProjection = previousProjection,
			projection = projection
				?: previousProjection
				?: error("Change of aggregate ${event.aggregateId} cannot transition from one non-existent projection " +
					"to another non-existent projection: $event"),
			timestamp = event.timestamp,
			version = event.version,
		)
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
