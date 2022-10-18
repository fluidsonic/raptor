package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateProjectionEventStream : RaptorAggregateProjectionEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateProjectionEventBatch<*, *, *>>()
	private val stopEvent = dummyAggregateProjectionEventBatch()


	suspend fun add(batch: RaptorAggregateProjectionEventBatch<*, *, *>) {
		flow.emit(batch)
	}


	override fun asBatchFlow(): Flow<RaptorAggregateProjectionEventBatch<*, *, *>> =
		flow
			.takeWhile { it !== stopEvent }
			.filterNot { it.isDummy() }


	@OptIn(FlowPreview::class)
	override fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>> =
		flow.flatMapConcat { batch ->
			batch.events.asFlow()
		}


	suspend fun stop() {
		// TODO This doesn't actually wait for events to be processed.
		wait()

		flow.emit(stopEvent)
	}


	override suspend fun wait() {
		val waitEvent = dummyAggregateProjectionEventBatch()

		coroutineScope {
			flow
				.onStart { launch { flow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}
}
