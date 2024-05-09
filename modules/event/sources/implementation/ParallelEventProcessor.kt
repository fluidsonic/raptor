package io.fluidsonic.raptor.event

import java.util.concurrent.*
import kotlin.reflect.*
import kotlinx.coroutines.*


public class ParallelEventProcessor : RaptorEventProcessor, RaptorEventSource {

	private val subscriptionsByEvent: MutableMap<KClass<out RaptorEvent>, MutableList<Subscription<*>>> = ConcurrentHashMap()


	override suspend fun process(event: RaptorEvent) {
		@Suppress("UNCHECKED_CAST")
		val subscriptions = subscriptionsByEvent[event::class] as List<Subscription<RaptorEvent>>? ?: return

		when (val size = subscriptions.size) {
			0 -> return
			1 -> subscriptions[0].handle(event)?.join()
			else -> subscriptions
				.mapNotNullTo(ArrayList(size)) { it.handle(event) }
				.forEach { it.join() }
		}
	}


	override fun <Event : RaptorEvent> subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: Event) -> Unit,
		events: Set<KClass<out Event>>,
		async: Boolean,
	): Job {
		val job = Job(parent = scope.coroutineContext.job)
		if (events.isEmpty())
			return job

		val subscriptions = events.map { eventClass ->
			Subscription(
				async = async,
				eventClass = eventClass,
				handler = handler,
				job = job,
				scope = scope,
			).also { subscription ->
				subscriptionsByEvent.computeIfAbsent(eventClass) { CopyOnWriteArrayList() }.add(subscription)
			}
		}

		job.invokeOnCompletion {
			for (subscription in subscriptions)
				subscriptionsByEvent[subscription.eventClass]?.remove(subscription)
		}

		return job
	}


	private class Subscription<Event : RaptorEvent>(
		private val async: Boolean,
		val eventClass: KClass<Event>,
		private val handler: suspend (event: Event) -> Unit,
		private val job: Job,
		private val scope: CoroutineScope,
	) {

		fun isCanceled() =
			job.isCompleted


		// 'UNDISPATCHED' to allow the handler to maintain the order of events across coroutine launches.
		// 'join()' to suspend this method until the handler is complete to ensure sequential processing.
		fun handle(event: Event): Job? =
			scope.launch(start = CoroutineStart.UNDISPATCHED) {
				// Ensure that the subscription wasn't canceled in the meantime.
				if (!isCanceled())
					handler(event)
			}.takeUnless { async }
	}
}
