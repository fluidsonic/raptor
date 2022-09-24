package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// FIXME
public interface RaptorAggregateEventStream {

	public fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>>

	public fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> subscribeIn(
		scope: CoroutineScope,
		collector: suspend (event: RaptorEvent<Id, Event>) -> Unit,
		errorStrategy: ErrorStrategy,
		eventClass: KClass<Event>,
		idClass: KClass<Id>,
	): Job


	public enum class ErrorStrategy {

		skip,
		unsubscribeFromAll,
		unsubscribeFromProjection,
	}
}


@Suppress("UNCHECKED_CAST")
public fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>>
	Flow<RaptorEvent<*, *>>.filterIsInstance(
	idClass: KClass<Id>,
	eventClass: KClass<Event>,
): Flow<RaptorEvent<Id, Event>> =
	filter { event ->
		idClass.isInstance(event.aggregateId) && eventClass.isInstance(event.data)
	} as Flow<RaptorEvent<Id, Event>>


public inline fun <reified Id : RaptorAggregateId, reified Event : RaptorAggregateEvent<Id>>
	Flow<RaptorEvent<*, *>>.filterIsInstance(): Flow<RaptorEvent<Id, Event>> =
	filterIsInstance(idClass = Id::class, eventClass = Event::class)


public inline fun <reified Id : RaptorAggregateId, reified Event : RaptorAggregateEvent<Id>>
	RaptorAggregateEventStream.subscribeIn(
	scope: CoroutineScope,
	noinline collector: suspend (event: RaptorEvent<Id, Event>) -> Unit,
	errorStrategy: RaptorAggregateEventStream.ErrorStrategy,
): Job =
	subscribeIn(
		scope = scope,
		collector = collector,
		errorStrategy = errorStrategy,
		eventClass = Event::class,
		idClass = Id::class,
	)
