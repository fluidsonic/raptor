package io.fluidsonic.raptor.domain

import io.fluidsonic.time.Timestamp
import kotlinx.coroutines.flow.*


public interface RaptorAggregateStore {

	public suspend fun add(events: List<RaptorAggregateEvent<*, *>>)
	public suspend fun lastEventTimestampOrNull(): Timestamp?
	public fun load(after: RaptorAggregateEventId? = null): Flow<RaptorAggregateEvent<*, *>>

	public suspend fun start() {}


	public companion object
}
