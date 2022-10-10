package io.fluidsonic.raptor.cqrs

import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateEventStream : RaptorAggregateEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateEvent<*, *>>()


	suspend fun add(event: RaptorAggregateEvent<*, *>) {
		flow.emit(event)
	}


	override fun asFlow(): Flow<RaptorAggregateEvent<*, *>> =
		flow
}
