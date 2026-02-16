package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateStream : RaptorAggregateStream {

	private val liveFlow = MutableSharedFlow<RaptorAggregateStreamMessage<*, *>>()
	private val replayFlow = MutableSharedFlow<RaptorAggregateStreamMessage<*, *>>(extraBufferCapacity = 64)
	private val stopMessage = RaptorAggregateStreamMessage.Other(Ping)
	private val switchSentinel = RaptorAggregateStreamMessage.Other(Switch)

	private var activeFlow: MutableSharedFlow<RaptorAggregateStreamMessage<*, *>> = replayFlow

	override val messages: Flow<RaptorAggregateStreamMessage<*, *>> = flow {
		replayFlow
			.takeWhile { it !== switchSentinel }
			.collect { emit(it) }
		liveFlow
			.takeWhile { it !== stopMessage }
			.collect { emit(it) }
	}.filterNot { it is RaptorAggregateStreamMessage.Other && (it.value === Ping || it.value === Switch) }


	suspend fun emit(message: RaptorAggregateStreamMessage<*, *>) {
		// TODO Fail when stopped.
		activeFlow.emit(message)
	}


	suspend fun replayComplete() {
		val subscriberCount = replayFlow.subscriptionCount.value
		activeFlow = liveFlow
		replayFlow.emit(switchSentinel)
		// Wait until all subscribers have transitioned from replayFlow to liveFlow.
		if (subscriberCount > 0)
			liveFlow.subscriptionCount.first { it >= subscriberCount }
	}


	suspend fun stop() {
		// TODO Make idempotent.

		coroutineScope {
			liveFlow
				.onStart { launch { liveFlow.emit(stopMessage) } }
				.firstOrNull { it === stopMessage }
		}
	}


	override suspend fun wait() {
		val waitEvent = RaptorAggregateStreamMessage.Other(Ping)

		coroutineScope {
			activeFlow
				.onStart { launch { activeFlow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}


	private object Ping
	private object Switch
}
