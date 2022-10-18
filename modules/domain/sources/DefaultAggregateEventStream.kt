package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateEventStream : RaptorAggregateEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateEventBatch<*, *>>()
	private val stopEvent = dummyAggregateEventBatch()


	suspend fun add(batch: RaptorAggregateEventBatch<*, *>) {
		flow.emit(batch)
	}


	override fun asBatchFlow(): Flow<RaptorAggregateEventBatch<*, *>> =
		flow
			.takeWhile { it !== stopEvent }
			.filterNot { it.isDummy() }


	// TODO Collects forever if called after stop().
	@OptIn(FlowPreview::class)
	override fun asFlow(): Flow<RaptorAggregateEvent<*, *>> =
		flow.flatMapConcat { batch ->
			batch.events.asFlow()
		}


	suspend fun stop() {
		// TODO This doesn't actually wait for events to be processed.
		wait()

		flow.emit(stopEvent)
	}


	override suspend fun wait() {
		val waitEvent = dummyAggregateEventBatch()

		coroutineScope {
			flow
				.onStart { launch { flow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}
}
