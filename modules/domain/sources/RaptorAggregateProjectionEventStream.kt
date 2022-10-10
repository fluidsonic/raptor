package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


public interface RaptorAggregateProjectionEventStream {

	public fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>>


	// TODO Share with non-projection stream
	public enum class ErrorStrategy {

		skip,
		unsubscribeFromAll,
		unsubscribeFromProjection,
	}
}


public fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	collector: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
	errorStrategy: RaptorAggregateProjectionEventStream.ErrorStrategy, // FIXME use
	changeClass: KClass<Change>,
	idClass: KClass<Id>,
	projectionClass: KClass<Projection>,
): Job {
	var failedProjectionIds: MutableSet<RaptorAggregateProjectionId>? = null

	return asFlow()
		.filterIsInstance(changeClass = changeClass, idClass = idClass, projectionClass = projectionClass)
		.onEach { event ->
			val projectionId = event.projection.id

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
		.launchIn(scope)
}


public inline fun <reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, reified Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
	errorStrategy: RaptorAggregateProjectionEventStream.ErrorStrategy,
): Job =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
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
		val projection: RaptorProjection<*> = event.projection

		changeClass.isInstance(event.change) && idClass.isInstance(projection.id) && projectionClass.isInstance(projection)
	} as Flow<RaptorAggregateProjectionEvent<Id, Projection, Change>>


public inline fun <reified Id : RaptorAggregateProjectionId, reified Change : RaptorAggregateChange<Id>, reified Projection : RaptorProjection<Id>>
	Flow<RaptorAggregateProjectionEvent<*, *, *>>.filterIsInstance(): Flow<RaptorAggregateProjectionEvent<Id, Projection, Change>> =
	filterIsInstance(changeClass = Change::class, idClass = Id::class, projectionClass = Projection::class)
