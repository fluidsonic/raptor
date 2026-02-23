package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.*
import java.util.concurrent.atomic.*


// FIXME Make sure all subscribers subscribe before the first events are emitted.
// FIXME Error handling? Make sure Flow never stops.
internal class DefaultAggregateStream : RaptorAggregateStream {
	private val liveFlow = MutableSharedFlow<RaptorAggregateStreamMessage<*, *>>()
	private val logger: Logger = LoggerFactory.getLogger(DefaultAggregateStream::class.java)
	private val replayData = CompletableDeferred<RaptorAggregateStreamMessage.BulkReplay?>()
	private val replayFlow = MutableSharedFlow<RaptorAggregateStreamMessage<*, *>>()
	private val stopMessage = RaptorAggregateStreamMessage.Other(Ping)
	private val subscriberCount = AtomicInteger(0)
	private var subscriberCountLogged = false
	private val switchSentinel = RaptorAggregateStreamMessage.Other(Switch)

	override val messages: Flow<RaptorAggregateStreamMessage<*, *>> = flow {
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
	}.filterNot { it is RaptorAggregateStreamMessage.Other && (it.value === Ping || it.value === Switch) }


	suspend fun awaitReplayProcessing() {
		val expected = subscriberCount.get()
		System.err.println("[boot-profiling] EventStream: waiting for $expected subscribers to finish replay processing")
		if (expected > 0)
			replayFlow.subscriptionCount.first { it >= expected }
		System.err.println("[boot-profiling] EventStream: all $expected subscribers ready")
	}


	suspend fun emit(message: RaptorAggregateStreamMessage<*, *>) {
		// TODO Fail when stopped.
		if (!subscriberCountLogged) {
			subscriberCountLogged = true
			System.err.println("[boot-profiling] EventStream: ${liveFlow.subscriptionCount.value} active subscribers at first live emit")
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
