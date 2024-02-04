package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorAggregateProjectionStream {

	public fun <
		Id : RaptorAggregateProjectionId,
		Change : RaptorAggregateChange<Id>,
		Projection : RaptorAggregateProjection<Id>,
		>
		subscribeIn(
		scope: CoroutineScope,
		changeClass: KClass<Change>,
		idClass: KClass<Id>,
		projectionClass: KClass<Projection>,
		handle: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
		includeReplay: Boolean = false,
	): Job

	public fun subscribeReplayCompletedIn(
		scope: CoroutineScope,
		handle: suspend () -> Unit,
	): Job
}


public inline fun <
	reified Id : RaptorAggregateProjectionId,
	reified Change : RaptorAggregateChange<Id>,
	reified Projection : RaptorAggregateProjection<Id>,
	>
	RaptorAggregateProjectionStream.subscribeIn(
	scope: CoroutineScope,
	noinline handle: suspend (event: RaptorAggregateProjectionEvent<Id, Projection, Change>) -> Unit,
	includeReplay: Boolean = false,
	changes: Set<KClass<out Change>> = setOf(Change::class), // FIXME
): Job =
	subscribeIn(
		changeClass = Change::class,
		handle = handle,
		idClass = Id::class,
		includeReplay = includeReplay,
		projectionClass = Projection::class,
		scope = scope,
	)
