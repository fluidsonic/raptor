package io.fluidsonic.raptor.cqrs

import kotlinx.coroutines.flow.*


internal interface RaptorAggregateStore {

	suspend fun add(events: List<RaptorEvent<*, *>>)
	fun load(): Flow<RaptorEvent<*, *>>
}
