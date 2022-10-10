package io.fluidsonic.raptor.cqrs

import kotlinx.coroutines.flow.*


public interface RaptorAggregateStore {

	public suspend fun add(events: List<RaptorAggregateEvent<*, *>>)
	public fun load(): Flow<RaptorAggregateEvent<*, *>>
}
