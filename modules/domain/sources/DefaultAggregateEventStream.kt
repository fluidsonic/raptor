package io.fluidsonic.raptor.cqrs

import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


internal class DefaultAggregateEventStream : RaptorAggregateEventStream {

	override fun asFlow(): Flow<RaptorAggregateProjectionEvent<*, *, *>> {
		return flow { } // FIXME
	}

	override fun <Id : RaptorAggregateId, Event : RaptorAggregateEvent<Id>> subscribeIn(scope: CoroutineScope, collector: suspend (event: RaptorEvent<Id, Event>) -> Unit, errorStrategy: RaptorAggregateEventStream.ErrorStrategy, eventClass: KClass<Event>, idClass: KClass<Id>): Job {
		// FIXME
		return Job()
	}
}
