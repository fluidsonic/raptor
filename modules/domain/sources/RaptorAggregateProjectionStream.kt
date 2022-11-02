// FIXME Needs refactoring.

package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


public interface RaptorAggregateProjectionEventStream {

	public val messages: Flow<RaptorAggregateProjectionStreamMessage<*, *, *>>

	public suspend fun wait()
}


@OptIn(FlowPreview::class)
public fun <
	ProjectionId : RaptorAggregateProjectionId,
	Projection : RaptorProjection<ProjectionId>,
	Change : RaptorAggregateChange<ProjectionId>,
	> Flow<RaptorAggregateProjectionStreamMessage<ProjectionId, Projection, Change>>.events(): Flow<RaptorAggregateProjectionEvent<ProjectionId, Projection, Change>> =
	flatMapConcat { message ->
		when (message) {
			is RaptorAggregateProjectionEventBatch<ProjectionId, Projection, Change> -> message.events.asFlow()
			else -> emptyFlow()
		}
	}


@JvmName("subscribeBatchIn")
@Suppress("UNCHECKED_CAST")
public suspend fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	collector: suspend (event: RaptorAggregateProjectionEventBatch<Id, Projection, Change>) -> Unit,
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
	includeReplays: Boolean = false,
): Job {
	val completion = CompletableDeferred<Unit>()
	var failedProjectionIds: MutableSet<RaptorAggregateProjectionId>? = null

	return messages
		.let { flow ->
			when (includeReplays) {
				true -> flow
				false -> flow.dropWhile { it !is RaptorAggregateProjectionStreamMessage.Loaded }
			}
		}
		.filterIsInstance<RaptorAggregateProjectionEventBatch<*, *, *>>()
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
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
	includeReplays: Boolean = false,
): Job {
	val completion = CompletableDeferred<Unit>()
	var failedProjectionIds: MutableSet<RaptorAggregateProjectionId>? = null

	return messages
		.let { flow ->
			when (includeReplays) {
				true -> flow
				false -> flow.dropWhile { it !is RaptorAggregateProjectionStreamMessage.Loaded }
			}
		}
		.events()
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
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip,
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
	errorStrategy: RaptorAggregateStream.ErrorStrategy = RaptorAggregateStream.ErrorStrategy.skip,
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
public suspend fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeMessagesIn(
	scope: CoroutineScope,
	collector: suspend (message: RaptorAggregateProjectionStreamMessage<Id, Projection, Change>) -> Unit,
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
): Job {
	val completion = CompletableDeferred<Unit>()

	return messages
		.mapNotNull { message ->
			when (message) {
				is RaptorAggregateProjectionEventBatch<*, *, *> ->
					message
						.takeIf { idClass.isInstance(it.projectionId) }
						?.events
						?.mapNotNull { it.castOrNull(changeClass = changeClass, idClass = idClass, projectionClass = projectionClass) }
						?.ifEmpty { null }
						?.let { message.copy(events = it as List<RaptorAggregateProjectionEvent<Nothing, Nothing, Nothing>>) }

				else -> message
			}
		}
		.let { it as Flow<RaptorAggregateProjectionStreamMessage<Id, Projection, Change>> }
		.onEach { event ->
			collector(event)
		}
		.onStart { completion.complete(Unit) }
		.launchIn(scope)
		.also { completion.await() }
}


public suspend inline fun <reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, reified Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeMessagesIn(
	scope: CoroutineScope,
	noinline collector: suspend (message: RaptorAggregateProjectionStreamMessage<Id, Projection, Change>) -> Unit,
): Job =
	subscribeMessagesIn(
		scope = scope,
		collector = collector,
		changeClass = Change::class,
		idClass = Id::class,
		projectionClass = Projection::class,
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
