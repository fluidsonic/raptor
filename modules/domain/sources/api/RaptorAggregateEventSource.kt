package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorAggregateEventSource {

	context(coroutineScope: CoroutineScope)
	public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> subscribe(
		handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
		changeClasses: Set<KClass<out Change>>,
		idClass: KClass<Id>,
		async: Boolean = false,
		replay: Boolean = false,
	): Job


	context(coroutineScope: CoroutineScope)
	public fun subscribe(
		handler: suspend (event: RaptorAggregateReplayCompletedEvent) -> Unit,
		async: Boolean = false,
	): Job
}


public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorAggregateEventSource.subscribeIn(
	scope: CoroutineScope,
	noinline handler: suspend (event: RaptorAggregateEvent<Id, Change>) -> Unit,
	changeClasses: Set<KClass<out Change>> = setOf(Change::class),
	async: Boolean = false,
	replay: Boolean = false,
): Job =
	context(scope) {
		subscribe(handler, changeClasses, Id::class, async = async, replay = replay)
	}
