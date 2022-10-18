package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


public interface RaptorAggregateProjectionEventStream {

	public fun asBatchFlow(): Flow<RaptorAggregateProjectionEventBatch<*, *, *>>
	public fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>>
	public suspend fun wait()
}


@JvmName("subscribeBatchIn")
@Suppress("UNCHECKED_CAST")
public suspend fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	collector: suspend (event: RaptorAggregateProjectionEventBatch<Id, Projection, Change>) -> Unit,
	errorStrategy: RaptorAggregateEventStream.ErrorStrategy = RaptorAggregateEventStream.ErrorStrategy.skip, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
	includeReplays: Boolean = false,
): Job {
	val completion = CompletableDeferred<Unit>()
	var failedProjectionIds: MutableSet<RaptorAggregateProjectionId>? = null

	return asBatchFlow()
		.let { flow ->
			when (includeReplays) {
				true -> flow
				false -> flow.filter { !it.isReplay }
			}
		}
		.filter { idClass.isInstance(it.projectionId) }
		.mapNotNull { batch ->
			batch.events
				.mapNotNull { it.castOrNull(changeClass = changeClass, idClass = idClass, projectionClass = projectionClass) }
				.ifEmpty { null }
				?.let { batch.copy(events = it as List<RaptorAggregateProjectionEvent<Nothing, Nothing, Nothing>>) }
		}
		.let { it as Flow<RaptorAggregateProjectionEventBatch<Id, Projection, Change>> }
		.onEach { event ->
			val projectionId = event.projectionId

			if (failedProjectionIds?.contains(projectionId) == true)
				return@onEach

			try {
				collector(event)
			}
			catch (e: CancellationException) {
				throw e
			}
			catch (e: Throwable) {
				(failedProjectionIds ?: hashSetOf<RaptorAggregateProjectionId>().also { failedProjectionIds = it })
					.add(projectionId)
			}
		}
		.onStart { completion.complete(Unit) }
		.launchIn(scope)
		.also { completion.await() }
}


public suspend fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	collector: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
	errorStrategy: RaptorAggregateEventStream.ErrorStrategy = RaptorAggregateEventStream.ErrorStrategy.skip, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
	includeReplays: Boolean = false,
): Job {
	val completion = CompletableDeferred<Unit>()
	var failedProjectionIds: MutableSet<RaptorAggregateProjectionId>? = null

	return asFlow()
		.let { flow ->
			when (includeReplays) {
				true -> flow
				false -> flow.filter { !it.isReplay }
			}
		}
		.filterIsInstance(changeClass = changeClass, idClass = idClass, projectionClass = projectionClass)
		.onEach { event ->
			val projectionId = event.projectionId

			if (failedProjectionIds?.contains(projectionId) == true)
				return@onEach

			try {
				collector(event)
			}
			catch (e: CancellationException) {
				throw e
			}
			catch (e: Throwable) {
				(failedProjectionIds ?: hashSetOf<RaptorAggregateProjectionId>().also { failedProjectionIds = it })
					.add(projectionId)
			}
		}
		.onStart { completion.complete(Unit) }
		.launchIn(scope)
		.also { completion.await() }
}


@JvmName("subscribeBatchIn")
public suspend inline fun <reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, reified Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorAggregateProjectionEventBatch<Id, Projection, Change>) -> Unit,
	errorStrategy: RaptorAggregateEventStream.ErrorStrategy = RaptorAggregateEventStream.ErrorStrategy.skip,
	includeReplays: Boolean = false,
): Job =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
		changeClass = Change::class,
		idClass = Id::class,
		projectionClass = Projection::class,
		includeReplays = includeReplays,
	)


public suspend inline fun <reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, reified Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
	errorStrategy: RaptorAggregateEventStream.ErrorStrategy = RaptorAggregateEventStream.ErrorStrategy.skip,
	includeReplays: Boolean = false,
): Job =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
		changeClass = Change::class,
		idClass = Id::class,
		projectionClass = Projection::class,
		includeReplays = includeReplays,
	)


@Suppress("UNCHECKED_CAST")
public fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorProjection<Id>>
	Flow<RaptorAggregateProjectionEvent<*, *, *>>.filterIsInstance(
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
): Flow<RaptorAggregateProjectionEvent<Id, Projection, Change>> =
	filter { event ->
		val previousProjection: RaptorProjection<*>? = event.previousProjection
		val projection: RaptorProjection<*>? = event.projection

		changeClass.isInstance(event.change)
			&& (previousProjection == null || idClass.isInstance(previousProjection.id))
			&& (projection == null || idClass.isInstance(projection.id))
			&& (previousProjection == null || projectionClass.isInstance(previousProjection))
			&& (projection == null || projectionClass.isInstance(projection))
	} as Flow<RaptorAggregateProjectionEvent<Id, Projection, Change>>


public inline fun <reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, reified Projection : RaptorProjection<Id>>
	Flow<RaptorAggregateProjectionEvent<*, *, *>>.filterIsInstance(): Flow<RaptorAggregateProjectionEvent<Id, Projection, Change>> =
	filterIsInstance(changeClass = Change::class, idClass = Id::class, projectionClass = Projection::class)
