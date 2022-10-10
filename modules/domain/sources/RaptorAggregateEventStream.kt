package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


public interface RaptorAggregateEventStream {

	public fun asFlow(): Flow<RaptorAggregateEvent<*, *>>


	public enum class ErrorStrategy {

		skip,
		unsubscribeFromAll,
		unsubscribeFromProjection,
	}
}


public suspend fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
	RaptorAggregateEventStream.subscribeIn(
	scope: CoroutineScope,
	collector: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	errorStrategy: RaptorAggregateEventStream.ErrorStrategy = RaptorAggregateEventStream.ErrorStrategy.skip, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
): Job {
	val completion = CompletableDeferred<Unit>()
	var failedAggregateIds: MutableSet<RaptorAggregateId>? = null

	return asFlow()
		.filterIsInstance(changeClass = changeClass, idClass = idClass)
		.onEach { event ->
			val aggregateId = event.aggregateId

			if (failedAggregateIds?.contains(aggregateId) == true)
				return@onEach

			try {
				collector(event)
			}
			catch (e: CancellationException) {
				throw e
			}
			catch (e: Throwable) {
				(failedAggregateIds ?: hashSetOf<RaptorAggregateId>().also { failedAggregateIds = it })
					.add(aggregateId)
			}
		}
		.onStart { completion.complete(Unit) }
		.launchIn(scope)
		.also { completion.await() }
}


public suspend inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorAggregateEventStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	errorStrategy: RaptorAggregateEventStream.ErrorStrategy = RaptorAggregateEventStream.ErrorStrategy.skip,
): Job =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
		changeClass = Change::class,
		idClass = Id::class,
	)


@Suppress("UNCHECKED_CAST")
public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
	Flow<RaptorAggregateEvent<*, *>>.filterIsInstance(
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
): Flow<RaptorAggregateEvent<Id, Change>> =
	filter { event ->
		idClass.isInstance(event.aggregateId) && changeClass.isInstance(event.change)
	} as Flow<RaptorAggregateEvent<Id, Change>>


public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	Flow<RaptorAggregateEvent<*, *>>.filterIsInstance(): Flow<RaptorAggregateEvent<Id, Change>> =
	filterIsInstance(changeClass = Change::class, idClass = Id::class)
