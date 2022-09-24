package io.fluidsonic.raptor.cqrs

import kotlinx.coroutines.flow.*


// FIXME race
internal class MemoryAggregateStore : RaptorAggregateStore {

	private val events: MutableList<RaptorEvent<*, *>> = mutableListOf()


	override suspend fun add(events: List<RaptorEvent<*, *>>) {
		this.events += events
	}


	override fun load() =
		events.toList().asFlow()
}
