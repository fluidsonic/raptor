package io.fluidsonic.raptor.domain

import java.util.concurrent.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*


// TODO Needs refactoring. Use non-concurrent coroutines to avoid the use of ConcurrentHashMap(), CopyOnWriteArrayList(), and atomic().
internal class DefaultAggregateStream(
	private val definitions: RaptorAggregateDefinitions,
) : RaptorAggregateStream {

	private val phase = atomic(Phase.setup)

	private val replayCompletedSubscriptions: MutableList<ReplayCompletedSubscription> = CopyOnWriteArrayList()
	private val subscriptions: MutableMap<KClass<out RaptorAggregateChange<*>>, MutableList<Subscription<*, *>>> =
		ConcurrentHashMap()
	private val subscriptionsAfterReplay: MutableMap<KClass<out RaptorAggregateChange<*>>, MutableList<Subscription<*, *>>> =
		ConcurrentHashMap()


	suspend fun handleEvent(event: RaptorAggregateEvent<*, *>) {
		check(phase.value != Phase.setup) { "Can't handle events during setup phase." }

		val changeSubscriptions = subscriptions[event.change::class] ?: return

		@Suppress("UNCHECKED_CAST")
		event as RaptorAggregateEvent<Nothing, Nothing>

		// fixme error handling, also proj
		when (val singleSubscription = changeSubscriptions.singleOrNull()) {
			null -> coroutineScope {
				for (subscription in changeSubscriptions)
				// Start without dispatching to improve performance.
					launch(start = CoroutineStart.UNDISPATCHED) { subscription.handle(event) }
			}

			else -> singleSubscription.handle(event)
		}
	}


	suspend fun handleReplayCompleted() {
		check(phase.compareAndSet(expect = Phase.replay, update = Phase.live)) { "Can't complete replay in $phase phase." }

		// TODO Rework this. Race condition if a subscriber unsubscribes at the same time.
		subscriptions.clear()
		subscriptions.putAll(subscriptionsAfterReplay)
		subscriptionsAfterReplay.clear()

		val replayCompletedSubscriptions = replayCompletedSubscriptions.toList()
		this.replayCompletedSubscriptions.clear()

		// FIXME scope
		coroutineScope {
			for (subscription in replayCompletedSubscriptions)
				launch(start = CoroutineStart.UNDISPATCHED) { subscription.handle() }
		}
	}


	fun handleSetupCompleted() {
		check(phase.compareAndSet(expect = Phase.setup, update = Phase.replay)) { "Can't complete setup in $phase phase." }
	}


	// FIXME actually dispatch in scope (also prj stream)
	override fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> subscribeIn(
		scope: CoroutineScope,
		changeClass: KClass<Change>,
		idClass: KClass<Id>,
		handle: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
		includeReplay: Boolean,
	): Job {
		check(phase.value == Phase.setup) { "Can't subscribe after the stream started." }

		val aggregateDefinition =
			checkNotNull(definitions[idClass]) { "There's no aggregate defined for '${idClass.qualifiedName}'." }

		check(changeClass.isSubclassOf(aggregateDefinition.changeClass)) {
			"Cannot subscribe to changes of $changeClass for aggregate '${aggregateDefinition.discriminator}'. " +
				"It's not related to the aggregate's change ${aggregateDefinition.changeClass}."
		}

		@Suppress("UNCHECKED_CAST")
		val concreteChangeClasses = aggregateDefinition.changeDefinitions
			.filter { it.changeClass.isSubclassOf(changeClass) }
			.map { it.changeClass } as List<KClass<out Change>>
		check(concreteChangeClasses.isNotEmpty()) {
			"$changeClass is not registered for changes of aggregate '${aggregateDefinition.discriminator}'."
		}

		val newSubscriptions = mutableListOf<Subscription<Id, out Change>>()

		for (concreteChangeClass in concreteChangeClasses) {
			val subscription = Subscription(changeClass = concreteChangeClass, handler = handle)
			newSubscriptions += subscription

			subscriptionsAfterReplay.computeIfAbsent(concreteChangeClass) { CopyOnWriteArrayList() }.add(subscription)

			if (includeReplay)
				subscriptions.computeIfAbsent(concreteChangeClass) { CopyOnWriteArrayList() }.add(subscription)
		}

		val job = Job(parent = scope.coroutineContext.job)
		job.invokeOnCompletion {
			for (subscription in newSubscriptions) {
				subscription.unsubscribe()
				subscriptionsAfterReplay[subscription.changeClass]?.remove(subscription)
				subscriptions[subscription.changeClass]?.remove(subscription)
			}
		}

		return job
	}


	// FIXME scope
	override fun subscribeReplayCompletedIn(scope: CoroutineScope, handle: suspend () -> Unit): Job {
		check(phase.value == Phase.setup) { "Can't subscribe after the stream started." }

		val subscription = ReplayCompletedSubscription(handler = handle)
		replayCompletedSubscriptions += subscription

		val job = Job(parent = scope.coroutineContext.job)
		job.invokeOnCompletion {
			subscription.unsubscribe()
			replayCompletedSubscriptions.remove(subscription)
		}

		return job
	}


	private class ReplayCompletedSubscription(
		private val handler: suspend () -> Unit,
	) {

		private val subscribed = atomic(true)


		suspend fun handle() {
			if (!subscribed.value)
				return

			handler()
		}


		fun unsubscribe() {
			subscribed.value = false
		}
	}


	private class Subscription<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
		val changeClass: KClass<Change>,
		private val handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	) {

		private val subscribed = atomic(true)


		suspend fun handle(event: RaptorAggregateEvent<Id, Change>) {
			if (!subscribed.value)
				return

			handler(event)
		}


		fun unsubscribe() {
			subscribed.value = false
		}
	}


	enum class Phase {

		setup,
		replay,
		live,
	}
}
