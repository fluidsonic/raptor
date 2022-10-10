package io.fluidsonic.raptor.domain

import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateProjectionEventStream : RaptorAggregateProjectionEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateProjectionEvent<*, *, *>?>()


	suspend fun add(event: RaptorAggregateProjectionEvent<*, *, *>) {
		flow.emit(event)
	}


	@Suppress("UNCHECKED_CAST")
	override fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>> =
		flow.takeWhile { it != null } as Flow<RaptorAggregateProjectionEvent<*, *, *>>


	suspend fun stop() {
		// TODO Wait for processing.
		flow.emit(null)
	}
}
