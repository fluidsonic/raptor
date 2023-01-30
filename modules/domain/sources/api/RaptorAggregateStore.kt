package io.fluidsonic.raptor.domain

import kotlinx.coroutines.flow.*


public interface RaptorAggregateStore {

	public suspend fun add(events: List<RaptorAggregateEvent<*, *>>)
	public fun load(after: RaptorAggregateEventId? = null): Flow<RaptorAggregateEvent<*, *>>

	public suspend fun start() {}


	public companion object
}
