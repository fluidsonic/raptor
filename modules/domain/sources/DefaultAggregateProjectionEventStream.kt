package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateProjectionEventStream : RaptorAggregateProjectionEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateProjectionEvent<*, *, *>>()
	private val stopEvent = dummyAggregateProjectionEvent()


	suspend fun add(event: RaptorAggregateProjectionEvent<*, *, *>) {
		flow.emit(event)
	}


	override fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>> =
		flow
			.takeWhile { it !== stopEvent }
			.filterNot { it.isDummy() }


	suspend fun stop() {
		// TODO This doesn't actually wait for events to be processed.
		wait()

		flow.emit(stopEvent)
	}


	override suspend fun wait() {
		val waitEvent = dummyAggregateProjectionEvent()

		coroutineScope {
			flow
				.onStart { launch { flow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}
}
