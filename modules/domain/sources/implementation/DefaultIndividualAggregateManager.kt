package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*
import kotlinx.coroutines.sync.*


internal class DefaultIndividualAggregateManager<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
	private val eventStream: DefaultAggregateStream,
	private val store: RaptorIndividualAggregateStore<Id, Change>,
) : RaptorIndividualAggregateManager<Id, Change> {

	private val mutex = Mutex()


	override suspend fun commit(
		id: Id,
		expectedVersion: Int,
		changes: List<Change>,
		timestamp: Timestamp,
	): List<RaptorAggregateEvent<Id, Change>> {
		mutex.withLock {
			val lastVersion = store.load(id).lastOrNull()?.version ?: 0
			if (lastVersion != expectedVersion)
				throw RaptorAggregateVersionConflict(
					"Expected aggregate ${id.debug} at version $expectedVersion but encountered version $lastVersion.",
				)

			if (changes.isEmpty())
				return emptyList()

			val lastEventId = store.lastEventId()?.toLong() ?: 0
			val lastVersionInBatch = lastVersion + changes.size
			val events = changes.mapIndexed { index, change ->
				RaptorAggregateEvent(
					aggregateId = id,
					change = change,
					id = RaptorAggregateEventId(lastEventId + 1 + index),
					lastVersionInBatch = lastVersionInBatch,
					timestamp = timestamp,
					version = lastVersion + 1 + index,
				)
			}

			store.save(id, events)

			eventStream.emit(
				RaptorAggregateEventBatch(
					aggregateId = id,
					events = events,
					version = events.last().version,
				)
			)

			return events
		}
	}


	override suspend fun fetch(id: Id): List<RaptorAggregateEvent<Id, Change>> =
		mutex.withLock {
			store.load(id)
		}


	suspend fun load() {
		mutex.withLock {
			for (event in store.reload())
			// Note that we don't support batching here.
				eventStream.emit(
					RaptorAggregateEventBatch(
						aggregateId = event.aggregateId,
						events = listOf(event),
						version = event.version,
					)
				)
		}
	}
}
