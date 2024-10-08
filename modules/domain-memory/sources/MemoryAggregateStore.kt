package io.fluidsonic.raptor.domain.memory

import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.Timestamp
import kotlinx.coroutines.flow.*


// FIXME Prevent race conditions.
// FIXME Check for version conflicts.
private class MemoryAggregateStore : RaptorAggregateStore {

	private val events: MutableList<RaptorAggregateEvent<*, *>> = mutableListOf()


	override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
		this.events += events
	}


	override suspend fun lastEventTimestampOrNull(): Timestamp? =
		events.lastOrNull()?.timestamp


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
