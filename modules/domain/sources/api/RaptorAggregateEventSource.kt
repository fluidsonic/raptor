package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorAggregateEventSource {

	public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
		subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
		changeClasses: Set<KClass<Change>>,
		idClass: KClass<Id>,
		async: Boolean = false,
		replay: Boolean = false,
	): Job

	public fun subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: RaptorAggregateReplayCompletedEvent) -> Unit,
		async: Boolean = false,
	): Job
}


public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorAggregateEventSource.subscribeIn(
	scope: CoroutineScope,
	noinline handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	changeClasses: Set<KClass<Change>> = setOf(Change::class),
	async: Boolean = false,
	replay: Boolean = false,
): Job =
	subscribeIn(scope, handler, changeClasses, Id::class, async = async, replay = replay)
