package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.event.*
import java.util.concurrent.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*


// TODO Add lifecycle.
internal class DefaultAggregateEventProcessor(
	private val definitions: RaptorAggregateDefinitions,
	private val eventSource: RaptorEventSource,
) : RaptorAggregateEventSource {

	private val replayCompleted = atomic(false)
	private val subscriptionsByChange: MutableMap<KClass<out RaptorAggregateChange<*>>, MutableList<Subscription<*, *>>> =
		ConcurrentHashMap()
	private val subscriptionsByChangeAfterReplay: MutableMap<KClass<out RaptorAggregateChange<*>>, MutableList<Subscription<*, *>>> =
		ConcurrentHashMap()


	suspend fun handleEvent(event: RaptorAggregateEvent<*, *>) {
		val subscriptions = subscriptionsByChange[event.change::class] ?: return

		@Suppress("UNCHECKED_CAST")
		event as RaptorAggregateEvent<Nothing, Nothing>

		when (val size = subscriptions.size) {
			0 -> return
			1 -> subscriptions[0].handle(event)?.join()
			else -> subscriptions
				.mapNotNullTo(ArrayList(size)) { it.handle(event) }
				.forEach { it.join() }
		}
	}


	fun handleEvent(@Suppress("UNUSED_PARAMETER") event: RaptorAggregateReplayCompletedEvent) {
		check(replayCompleted.compareAndSet(expect = false, update = true)) { "Can't complete replay more than once." }

		subscriptionsByChange.clear()
		subscriptionsByChange.putAll(subscriptionsByChangeAfterReplay)
		subscriptionsByChangeAfterReplay.clear()

		// Remove subscriptions that were canceled but still copied above due to a race condition.
		for ((_, subscriptions) in subscriptionsByChange)
			for (subscription in subscriptions)
				if (subscription.isCanceled())
					subscriptions.remove(subscription)
	}


	override fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
		changeClasses: Set<KClass<Change>>,
		idClass: KClass<Id>,
		async: Boolean,
		replay: Boolean,
	): Job {
		// TODO Add safeguard against adding subscribers after event emission has begun.

		val job = Job(parent = scope.coroutineContext.job)
		if (changeClasses.isEmpty())
			return job

		val aggregateDefinition =
			checkNotNull(definitions[idClass]) { "There's no aggregate defined for '${idClass.qualifiedName}'." }

		for (changeClass in changeClasses) {
			check(changeClass.isSubclassOf(aggregateDefinition.changeClass)) {
				"Cannot subscribe to changes of $changeClass for aggregate '${aggregateDefinition.discriminator}'. " +
					"It's not related to the aggregate's change ${aggregateDefinition.changeClass}."
			}
			check(aggregateDefinition.changeDefinitions.any { it.changeClass.isSubclassOf(changeClass) }) {
				"$changeClass is not registered for changes of aggregate '${aggregateDefinition.discriminator}'."
			}
		}

		@Suppress("UNCHECKED_CAST")
		val concreteChangeClasses = aggregateDefinition.changeDefinitions
			.filter { definition ->
				changeClasses.any { definition.changeClass.isSubclassOf(it) }
			}
			.map { it.changeClass } as List<KClass<out Change>>

		val subscriptions = concreteChangeClasses.map { changeClass ->
			Subscription(
				async = async,
				changeClass = changeClass,
				handler = handler,
				job = job,
				scope = scope,
			).also { subscription ->
				subscriptionsByChangeAfterReplay.computeIfAbsent(changeClass) { CopyOnWriteArrayList() }.add(subscription)

				if (replay)
					subscriptionsByChange.computeIfAbsent(changeClass) { CopyOnWriteArrayList() }.add(subscription)
			}
		}

		job.invokeOnCompletion {
			for (subscription in subscriptions) {
				subscriptionsByChangeAfterReplay[subscription.changeClass]?.remove(subscription)
				subscriptionsByChange[subscription.changeClass]?.remove(subscription)
			}
		}

		return job
	}


	override fun subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: RaptorAggregateReplayCompletedEvent) -> Unit,
		async: Boolean,
	): Job =
		eventSource.subscribeIn(scope, handler, async = async)


	private class Subscription<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
		private val async: Boolean,
		val changeClass: KClass<Change>,
		private val handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
		private val job: Job,
		private val scope: CoroutineScope,
	) {

		fun isCanceled() =
			job.isCompleted


		// 'UNDISPATCHED' to allow the handler to maintain the order of events across coroutine launches.
		// 'join()' to suspend this method until the handler is complete to ensure sequential processing.
		fun handle(event: RaptorAggregateEvent<Id, Change>): Job? =
			scope.launch(start = CoroutineStart.UNDISPATCHED) {
				// Ensure that the subscription wasn't canceled in the meantime.
				if (!isCanceled())
					handler(event)
			}.takeUnless { async }
	}
}
