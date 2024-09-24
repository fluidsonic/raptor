package io.fluidsonic.raptor.domain.memory

import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.*
import kotlinx.coroutines.sync.*


private class MemoryIndividualAggregateStore<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> :
	RaptorIndividualAggregateStore<Id, Change> {

	private val cache: MutableMap<Id, List<RaptorAggregateEvent<Id, Change>>> = hashMapOf()
	private var lastEventId: Long = 0
	private val mutex = Mutex()


	private fun appendToCache(id: Id, events: List<RaptorAggregateEvent<Id, Change>>) {
		cache.compute(id) { _, previousEvents ->
			previousEvents.orEmpty() + events
		}

		lastEventId = events.last().id.toLong()
	}


	override suspend fun load(id: Id): List<RaptorAggregateEvent<Id, Change>> =
		mutex.withLock {
			cache[id].orEmpty()
		}


	override suspend fun preload() {}


	override suspend fun save(id: Id, expectedVersion: Int, changes: List<Change>, timestamp: Timestamp) {
		mutex.withLock {
			val lastVersion = cache[id]?.lastOrNull()?.version ?: 0
			if (lastVersion != expectedVersion)
				throw RaptorAggregateVersionConflict(
					"Expected aggregate ${id.debug} at version $expectedVersion but encountered version $lastVersion.",
				)

			if (changes.isEmpty())
				return

			val lastEventId = lastEventId

			appendToCache(id, changes.mapIndexed { index, change ->
				RaptorAggregateEvent(
					aggregateId = id,
					change = change,
					id = RaptorAggregateEventId(lastEventId + 1 + index),
					timestamp = timestamp,
					version = lastVersion + 1 + index,
				)
			})
		}
	}
}


public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
	RaptorIndividualAggregateStore.Companion.memory(): RaptorIndividualAggregateStore<Id, Change> =
	MemoryIndividualAggregateStore()
