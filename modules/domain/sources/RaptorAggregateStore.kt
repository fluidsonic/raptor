package io.fluidsonic.raptor.domain

import kotlinx.coroutines.flow.*


public interface RaptorAggregateStore {

	public suspend fun add(events: List<RaptorAggregateEvent<*, *>>)
	public fun load(): Flow<RaptorAggregateEvent<*, *>>


	public companion object
}
