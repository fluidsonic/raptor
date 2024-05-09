package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorAggregateProjectionEventSource {

	public fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorAggregateProjection<Id>>
		subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
		changeClasses: Set<KClass<Change>>,
		idClass: KClass<Id>,
		projectionClass: KClass<Projection>,
		async: Boolean = false,
		replay: Boolean = false,
	): Job

	public fun subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: RaptorAggregateReplayCompletedEvent) -> Unit,
		async: Boolean = false,
	): Job
}


public inline fun <
	reified Id : RaptorAggregateProjectionId,
	reified Change : RaptorAggregateChange<Id>,
	reified Projection : RaptorAggregateProjection<Id>,
	>
	RaptorAggregateProjectionEventSource.subscribeIn(
	scope: CoroutineScope,
	noinline handler: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
	changeClasses: Set<KClass<Change>> = setOf(Change::class),
	async: Boolean = false,
	replay: Boolean = false,
): Job =
	subscribeIn(scope, handler, changeClasses, Id::class, Projection::class, async = async, replay = replay)
