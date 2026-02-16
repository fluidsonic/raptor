package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateProjectionStream : RaptorAggregateProjectionStream {

	private val liveFlow = MutableSharedFlow<RaptorAggregateProjectionStreamMessage<*, *, *>>()
	private val replayFlow = MutableSharedFlow<RaptorAggregateProjectionStreamMessage<*, *, *>>(extraBufferCapacity = 64)
	private val stopMessage = RaptorAggregateProjectionStreamMessage.Other(Ping)
	private val switchSentinel = RaptorAggregateProjectionStreamMessage.Other(Switch)

	private var activeFlow: MutableSharedFlow<RaptorAggregateProjectionStreamMessage<*, *, *>> = replayFlow

	override val messages: Flow<RaptorAggregateProjectionStreamMessage<*, *, *>> = flow {
		replayFlow
			.takeWhile { it !== switchSentinel }
			.collect { emit(it) }
		liveFlow
			.takeWhile { it !== stopMessage }
			.collect { emit(it) }
	}.filterNot { it is RaptorAggregateProjectionStreamMessage.Other && (it.value === Ping || it.value === Switch) }


	suspend fun emit(message: RaptorAggregateProjectionStreamMessage<*, *, *>) {
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
		val waitEvent = RaptorAggregateProjectionStreamMessage.Other(Ping)

		coroutineScope {
			activeFlow
				.onStart { launch { activeFlow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}


	private object Ping
	private object Switch
}
