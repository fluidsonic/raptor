package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*
import kotlinx.coroutines.sync.*


internal class DefaultIndividualAggregateManager<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
	private val eventProcessor: DefaultAggregateEventProcessor,
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

			val lastEventId = store.lastEventId()?.toLong() ?: 0 // FIXME race condition?
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

			for (event in events)
				eventProcessor.handleEvent(event)

			return events
		}
	}


	override suspend fun fetch(id: Id): List<RaptorAggregateEvent<Id, Change>> =
		mutex.withLock {
			store.load(id)
		}


	// FIXME document, maybe rename
	suspend fun load() {
		mutex.withLock {
			for (event in store.reload())
				eventProcessor.handleEvent(event)
		}
	}
}
