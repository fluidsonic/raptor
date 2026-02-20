package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorAggregateProjectionEventSource {

	context(coroutineScope: CoroutineScope)
	public fun <Id : RaptorAggregateProjectionId, Change : RaptorAggregateChange<Id>, Projection : RaptorAggregateProjection<Id>> subscribe(
		handler: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
		changeClasses: Set<KClass<out Change>>,
		idClass: KClass<Id>,
		projectionClass: KClass<out Projection>,
		async: Boolean = false,
		replay: Boolean = false,
	): Job


	context(coroutineScope: CoroutineScope)
	public fun subscribe(
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
	changeClasses: Set<KClass<out Change>> = setOf(Change::class),
	async: Boolean = false,
	replay: Boolean = false,
): Job =
	context(scope) {
		subscribe(handler, changeClasses, Id::class, Projection::class, async = async, replay = replay)
	}
