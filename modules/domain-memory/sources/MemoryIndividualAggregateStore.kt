package io.fluidsonic.raptor.domain.memory

import io.fluidsonic.raptor.domain.*


private class MemoryIndividualAggregateStore<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> :
	RaptorIndividualAggregateStore<Id, Change> {

	private val cache: MutableMap<Id, List<RaptorAggregateEvent<Id, Change>>> = hashMapOf()
	private var lastEventId: RaptorAggregateEventId? = null


	private fun appendToCache(id: Id, events: List<RaptorAggregateEvent<Id, Change>>) {
		cache.compute(id) { _, previousEvents ->
			previousEvents.orEmpty() + events
		}

		lastEventId = events.last().id
	}


	override suspend fun lastEventId(): RaptorAggregateEventId? =
		lastEventId


	override suspend fun load(id: Id): List<RaptorAggregateEvent<Id, Change>> =
		cache[id].orEmpty()


	override suspend fun reload(): List<RaptorAggregateEvent<Id, Change>> {
		cache.clear()

		return emptyList()
	}


	override suspend fun save(id: Id, events: List<RaptorAggregateEvent<Id, Change>>) {
		if (events.isEmpty())
			return

		require(events.all { it.aggregateId == id }) { "All events must have aggregate id '$id': $events" }

		var minimumEventId = lastEventId?.toLong() ?: 0
		events.forEachIndexed { index, event ->
			require(event.id.toLong() > minimumEventId) { "Event ${index + 1} ID ${event.id} must be > $minimumEventId: $events" }
			minimumEventId = event.id.toLong()
		}

		appendToCache(id, events)
	}
}


public fun RaptorIndividualAggregateStore.Companion.memory(): RaptorIndividualAggregateStore<*, *> =
	MemoryIndividualAggregateStore<RaptorAggregateId, RaptorAggregateChange<RaptorAggregateId>>()


@JvmName("memoryGeneric")
@Suppress("UNCHECKED_CAST")
public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorIndividualAggregateStore.Companion.memory(): RaptorIndividualAggregateStore<Id, Change> =
	memory() as RaptorIndividualAggregateStore<Id, Change>
