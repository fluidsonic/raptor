package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateStream : RaptorAggregateStream {

	private val flow = MutableSharedFlow<RaptorAggregateStreamMessage<*, *>>()
	private val stopMessage = RaptorAggregateStreamMessage.Other(Ping)

	override val messages: Flow<RaptorAggregateStreamMessage<*, *>> =
		flow
			.takeWhile { it !== stopMessage }
			.filterNot { it is RaptorAggregateStreamMessage.Other && it.value === Ping }


	suspend fun emit(message: RaptorAggregateStreamMessage<*, *>) {
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
		val waitEvent = RaptorAggregateStreamMessage.Other(Ping)

		coroutineScope {
			flow
				.onStart { launch { flow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}


	private object Ping
}
