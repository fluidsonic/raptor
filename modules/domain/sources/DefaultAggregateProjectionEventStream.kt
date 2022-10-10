package io.fluidsonic.raptor.cqrs

import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateProjectionEventStream : RaptorAggregateProjectionEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateProjectionEvent<*, *, *>>()


	suspend fun add(event: RaptorAggregateProjectionEvent<*, *, *>) {
		flow.emit(event)
	}


	override fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>> =
		flow
}
