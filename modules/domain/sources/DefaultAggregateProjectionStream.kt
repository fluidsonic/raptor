package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateProjectionStream : RaptorAggregateProjectionEventStream {

	private val flow = MutableSharedFlow<RaptorAggregateProjectionStreamMessage<*, *, *>>()
	private val stopMessage = RaptorAggregateProjectionStreamMessage.Other(Ping)

	override val messages: Flow<RaptorAggregateProjectionStreamMessage<*, *, *>> =
		flow
			.takeWhile { it !== stopMessage }
			.filterNot { it is RaptorAggregateProjectionStreamMessage.Other && it.value === Ping }


	suspend fun emit(message: RaptorAggregateProjectionStreamMessage<*, *, *>) {
		// TODO Fail when stopped.
		flow.emit(message)
	}


	suspend fun stop() {
		// TODO Make idempotent.

		coroutineScope {
			flow
				.onStart { launch { flow.emit(stopMessage) } }
				.firstOrNull { it === stopMessage }
		}
	}


	override suspend fun wait() {
		val waitEvent = RaptorAggregateProjectionStreamMessage.Other(Ping)

		coroutineScope {
			flow
				.onStart { launch { flow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}


	private object Ping
}
