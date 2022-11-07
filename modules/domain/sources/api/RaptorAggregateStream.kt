package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


public interface RaptorAggregateStream {

	public val messages: Flow<RaptorAggregateStreamMessage<*, *>>

	public suspend fun wait()


	public enum class ErrorStrategy {

		skip,
		unsubscribeFromAll,
		unsubscribeFromProjection,
	}
}


@OptIn(FlowPreview::class)
public fun <
	AggregateId : RaptorAggregateId,
	Change : RaptorAggregateChange<AggregateId>,
	> Flow<RaptorAggregateStreamMessage<AggregateId, Change>>.events(): Flow<RaptorAggregateEvent<AggregateId, Change>> =
	flatMapConcat { message ->
		when (message) {
			is RaptorAggregateEventBatch<AggregateId, Change> -> message.events.asFlow()
			else -> emptyFlow()
		}
	}


@JvmName("subscribeBatchIn")
@Suppress("UNCHECKED_CAST")
public suspend fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
	RaptorAggregateStream.subscribeIn(
	scope: CoroutineScope,
	collector: suspend (event: RaptorAggregateEventBatch<Id, Change>) -> Unit,
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	includeReplays: Boolean = false,
): Job {
	val completion = CompletableDeferred<Unit>()
	var failedAggregateIds: MutableSet<RaptorAggregateId>? = null

	// FIXME Using a Flow means that the events are no longer processed synchronously. OK? Workarounds?
	return messages
		.let { flow ->
			when (includeReplays) {
				true -> flow
				false -> flow.dropWhile { it !is RaptorAggregateStreamMessage.Loaded }
			}
		}
		.filterIsInstance<RaptorAggregateEventBatch<*, *>>()
		.filter { idClass.isInstance(it.aggregateId) }
		.mapNotNull { batch ->
			when {
				batch.events.all { changeClass.isInstance(it.change) } -> batch
				else -> batch.copy(events = batch.events
					.filter { changeClass.isInstance(it.change) }
					.ifEmpty { return@mapNotNull null }
					as List<RaptorAggregateEvent<Nothing, Nothing>>
				)
			}
		}
		.let { it as Flow<RaptorAggregateEventBatch<Id, Change>> }
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


public suspend fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
	RaptorAggregateStream.subscribeIn(
	scope: CoroutineScope,
	collector: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	includeReplays: Boolean = false,
): Job {
	val completion = CompletableDeferred<Unit>()
	var failedAggregateIds: MutableSet<RaptorAggregateId>? = null

	// FIXME Using a Flow means that the events are no longer processed synchronously. OK? Workarounds?
	return messages
		.let { flow ->
			when (includeReplays) {
				true -> flow
				false -> flow.dropWhile { it !is RaptorAggregateStreamMessage.Loaded }
			}
		}
		.events()
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


@JvmName("subscribeBatchIn")
public suspend inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorAggregateStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorAggregateEventBatch<Id, Change>) -> Unit,
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip,
	includeReplays: Boolean = false,
): Job =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
		changeClass = Change::class,
		idClass = Id::class,
		includeReplays = includeReplays,
	)


public suspend inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorAggregateStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip,
	includeReplays: Boolean = false,
): Job =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
		changeClass = Change::class,
		idClass = Id::class,
		includeReplays = includeReplays,
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
