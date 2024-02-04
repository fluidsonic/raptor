package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorAggregateStream {

	public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
		subscribeIn(
		scope: CoroutineScope,
		changeClass: KClass<Change>,
		idClass: KClass<Id>,
		handle: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
		includeReplay: Boolean = false,
	): Job

	public fun subscribeReplayCompletedIn(
		scope: CoroutineScope,
		handle: suspend () -> Unit,
	): Job
}


public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorAggregateStream.subscribeIn(
	scope: CoroutineScope,
	noinline handle: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	includeReplay: Boolean = false,
	changes: Set<KClass<out Change>> = setOf(Change::class), // FIXME
): Job =
	subscribeIn(
		changeClass = Change::class,
		handle = handle,
		idClass = Id::class,
		includeReplay = includeReplay,
		scope = scope,
	)
