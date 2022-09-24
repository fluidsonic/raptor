package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


public interface RaptorAggregateProjectionEventStream {

	public fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>>

	public fun <Id, Event : RaptorAggregateEvent<Id>, Projection : RaptorProjection<Id>> subscribeIn(
		scope: CoroutineScope,
		collector: suspend (event: RaptorAggregateProjectionEvent<Id, Event, Projection>) -> Unit,
		errorStrategy: ErrorStrategy,
		eventClass: KClass<Event>,
		idClass: KClass<Id>,
		projectionClass: KClass<Projection>,
	): Job where Id : RaptorAggregateId, Id : RaptorProjectionId


	// TODO Share with non-projection stream
	public enum class ErrorStrategy {

		skip,
		unsubscribeFromAll,
		unsubscribeFromProjection,
	}
}


@Suppress("UNCHECKED_CAST")
public fun <Id, Event : RaptorAggregateEvent<Id>, Projection : RaptorProjection<Id>>
	Flow<RaptorAggregateProjectionEvent<*, *, *>>.filterIsInstance(
	idClass: KClass<Id>,
	eventClass: KClass<Event>,
	projectionClass: KClass<Projection>,
): Flow<RaptorAggregateProjectionEvent<Id, Event, Projection>>
	where Id : RaptorAggregateId, Id : RaptorProjectionId =
	filter { event ->
		val projection: RaptorProjection<*> = event.projection

		idClass.isInstance(projection.id) && eventClass.isInstance(event.data) && projectionClass.isInstance(projection)
	} as Flow<RaptorAggregateProjectionEvent<Id, Event, Projection>>


public inline fun <reified Id, reified Event : RaptorAggregateEvent<Id>, reified Projection : RaptorProjection<Id>>
	Flow<RaptorAggregateProjectionEvent<*, *, *>>.filterIsInstance(): Flow<RaptorAggregateProjectionEvent<Id, Event, Projection>>
	where Id : RaptorAggregateId, Id : RaptorProjectionId =
	filterIsInstance(idClass = Id::class, eventClass = Event::class, projectionClass = Projection::class)


public inline fun <reified Id, reified Event : RaptorAggregateEvent<Id>, reified Projection : RaptorProjection<Id>>
	RaptorAggregateProjectionEventStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorAggregateProjectionEvent<Id, Event, Projection>) -> Unit,
	errorStrategy: RaptorAggregateProjectionEventStream.ErrorStrategy,
): Job where Id : RaptorAggregateId, Id : RaptorProjectionId =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
		eventClass = Event::class,
		idClass = Id::class,
		projectionClass = Projection::class,
	)
