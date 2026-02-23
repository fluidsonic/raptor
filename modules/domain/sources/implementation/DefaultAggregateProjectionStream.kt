package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.*
import java.util.concurrent.atomic.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateProjectionStream : RaptorAggregateProjectionStream {
	private val liveFlow = MutableSharedFlow<RaptorAggregateProjectionStreamMessage<*, *, *>>()
	private val logger: Logger = LoggerFactory.getLogger(DefaultAggregateProjectionStream::class.java)
	private val replayData = CompletableDeferred<RaptorAggregateProjectionStreamMessage.BulkReplay?>()
	private val replayFlow = MutableSharedFlow<RaptorAggregateProjectionStreamMessage<*, *, *>>()
	private val stopMessage = RaptorAggregateProjectionStreamMessage.Other(Ping)
	private val subscriberCount = AtomicInteger(0)
	private var subscriberCountLogged = false
	private val switchSentinel = RaptorAggregateProjectionStreamMessage.Other(Switch)

	override val messages: Flow<RaptorAggregateProjectionStreamMessage<*, *, *>> = flow {
		subscriberCount.incrementAndGet()

		// Process replay data independently per subscriber (cold emission).
		val replay = replayData.await()
		if (replay != null) {
			emit(replay)
		}

		// Subscribe to replayFlow as a synchronization barrier.
		replayFlow
			.takeWhile { it !== switchSentinel }
			.collect { emit(it) }

		// Process live events.
		liveFlow
			.takeWhile { it !== stopMessage }
			.collect { emit(it) }
	}.filterNot { it is RaptorAggregateProjectionStreamMessage.Other && (it.value === Ping || it.value === Switch) }


	suspend fun awaitReplayProcessing() {
		val expected = subscriberCount.get()
		System.err.println("[boot-profiling] ProjectionStream: waiting for $expected subscribers to finish replay processing")
		if (expected > 0)
			replayFlow.subscriptionCount.first { it >= expected }
		System.err.println("[boot-profiling] ProjectionStream: all $expected subscribers ready")
	}


	suspend fun emit(message: RaptorAggregateProjectionStreamMessage<*, *, *>) {
		// TODO Fail when stopped.
		if (!subscriberCountLogged) {
			subscriberCountLogged = true
			System.err.println("[boot-profiling] ProjectionStream: ${liveFlow.subscriptionCount.value} active subscribers at first live emit")
		}
		liveFlow.emit(message)
	}


	suspend fun replayComplete() {
		val count = replayFlow.subscriptionCount.value
		replayFlow.emit(switchSentinel)
		// Wait until all subscribers have transitioned from replayFlow to liveFlow.
		if (count > 0)
			liveFlow.subscriptionCount.first { it >= count }
	}


	fun setReplayData(batches: List<RaptorAggregateProjectionEventBatch<*, *, *>>) {
		val replay = if (batches.isNotEmpty()) RaptorAggregateProjectionStreamMessage.BulkReplay(batches) else null
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
		val waitEvent = RaptorAggregateProjectionStreamMessage.Other(Ping)

		coroutineScope {
			liveFlow
				.onStart { launch { liveFlow.emit(waitEvent) } }
				.firstOrNull { it === waitEvent }
		}
	}


	private object Ping

	private object Switch
}
