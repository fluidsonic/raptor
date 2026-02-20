package io.fluidsonic.raptor.event

import kotlin.reflect.*
import kotlinx.coroutines.*


public interface RaptorEventSource {

	context(coroutineScope: CoroutineScope)
	public fun <Event : RaptorEvent> subscribe(
		handler: suspend (event: Event) -> Unit,
		events: Set<KClass<out Event>>,
		async: Boolean = false,
	): Job
}


context(coroutineScope: CoroutineScope)
public inline fun <reified Event : RaptorEvent> RaptorEventSource.subscribe(
	noinline handler: suspend (event: Event) -> Unit,
): Job =
	subscribe(handler, setOf(Event::class), async = false)


context(coroutineScope: CoroutineScope)
public inline fun <reified Event : RaptorEvent> RaptorEventSource.subscribe(
	noinline handler: suspend (event: Event) -> Unit,
	async: Boolean,
): Job =
	subscribe(handler, setOf(Event::class), async = async)
