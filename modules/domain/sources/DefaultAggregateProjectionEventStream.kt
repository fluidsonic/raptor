package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


internal class DefaultAggregateProjectionEventStream : RaptorAggregateProjectionEventStream {

	override fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>> {
		return flow { } // FIXME
	}

	override fun <Id, Event : RaptorAggregateEvent<Id>, Projection : RaptorProjection<Id>> subscribeIn(scope: CoroutineScope, collector: suspend (event: RaptorAggregateProjectionEvent<Id, Event, Projection>) -> Unit, errorStrategy: RaptorAggregateProjectionEventStream.ErrorStrategy, eventClass: KClass<Event>, idClass: KClass<Id>, projectionClass: KClass<Projection>): Job where Id : RaptorAggregateId, Id : RaptorProjectionId {
		return Job() // FIXME
	}
}
