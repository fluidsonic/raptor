package io.fluidsonic.raptor.event

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorEventSource {

	public fun <Event : RaptorEvent> subscribeIn(
		scope: CoroutineScope,
		handler: suspend (event: Event) -> Unit,
		events: Set<KClass<out Event>>,
	): Job
}


public inline fun <reified Event : RaptorEvent> RaptorEventSource.subscribeIn(
	scope: CoroutineScope,
	noinline handler: suspend (event: Event) -> Unit,
): Job =
	subscribeIn(scope, handler, setOf(Event::class))
