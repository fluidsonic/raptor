package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


internal class DefaultAggregateStream : RaptorAggregateStream {

	private val liveFlow = MutableSharedFlow<RaptorAggregateStreamMessage<*, *>>()
	private val replayData = CompletableDeferred<RaptorAggregateStreamMessage.BulkReplay?>()
	private val replayFlow = MutableSharedFlow<RaptorAggregateStreamMessage<*, *>>()
	private val stopMessage = RaptorAggregateStreamMessage.Other(Ping)
	private val subscriberCount = MutableStateFlow(0)
	private val switchSentinel = RaptorAggregateStreamMessage.Other(Switch)


	override val messages: Flow<RaptorAggregateStreamMessage<*, *>> = flow {
		subscriberCount.update { it + 1 }

		try {
			// Process replay data independently per subscriber (cold emission).
			// Unpack into individual batches for backwards compatibility with subscribers
			// that use `when (message)` without a BulkReplay branch.
			val replay = replayData.await()
			if (replay != null)
				for (batch in replay.batches)
					emit(batch)
		} catch (e: Throwable) {
			// Allow startup to continue when a non-critical subscriber fails during replay.
			subscriberCount.update { it - 1 }
			throw e
		}

		// Subscribe to replayFlow as a synchronization barrier.
		replayFlow
			.takeWhile { it !== switchSentinel }
			.collect { emit(it) }

		// Process live events.
		liveFlow
			.takeWhile { it !== stopMessage }
			.collect { emit(it) }
	}.filterNot { it is RaptorAggregateStreamMessage.Other && (it.value === Ping || it.value === Switch) }


	suspend fun awaitReplayProcessing() {
		// Wait until every subscriber that started replay has either
		// finished processing (parked on replayFlow) or failed (decremented subscriberCount).
		combine(subscriberCount, replayFlow.subscriptionCount) { expected, parked ->
			parked >= expected
		}.first { it }
	}


	suspend fun emit(message: RaptorAggregateStreamMessage<*, *>) {
		// TODO Fail when stopped.
		liveFlow.emit(message)
	}


	suspend fun replayComplete() {
		val count = replayFlow.subscriptionCount.value
		replayFlow.emit(switchSentinel)
		// Wait until all subscribers have transitioned from replayFlow to liveFlow.
		if (count > 0)
			liveFlow.subscriptionCount.first { it >= count }
	}


	fun setReplayData(batches: List<RaptorAggregateEventBatch<*, *>>) {
		val replay = if (batches.isNotEmpty()) RaptorAggregateStreamMessage.BulkReplay(batches) else null
		replayData.complete(replay)
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
			liveFlow
				.onStart { launch { liveFlow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}


	private object Ping

	private object Switch
}
