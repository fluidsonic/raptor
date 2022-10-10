package io.fluidsonic.raptor.domain

import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateEventStream : RaptorAggregateEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateEvent<*, *>?>()


	suspend fun add(event: RaptorAggregateEvent<*, *>) {
		flow.emit(event)
	}


	@Suppress("UNCHECKED_CAST")
	override fun asFlow(): Flow<RaptorAggregateEvent<*, *>> =
		flow.takeWhile { it != null } as Flow<RaptorAggregateEvent<*, *>>


	suspend fun stop() {
		// TODO Wait for processing.
		flow.emit(null)
	}
}
