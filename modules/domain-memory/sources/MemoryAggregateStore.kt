package io.fluidsonic.raptor.domain

import kotlinx.coroutines.flow.*


// FIXME Prevent race conditions.
// FIXME Check for version conflicts.
private class MemoryAggregateStore : RaptorAggregateStore {

	private val events: MutableList<RaptorAggregateEvent<*, *>> = mutableListOf()


	override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
		this.events += events
	}


	override fun load(after: RaptorAggregateEventId?) =
		events
			.let { events ->
				when (after) {
					null -> events.toList()
					else -> events.filter { it.id > after }
				}
			}
			.asFlow()
}


public fun RaptorAggregateStore.Companion.memory(): RaptorAggregateStore =
	MemoryAggregateStore()
