package io.fluidsonic.raptor.event

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.lifecycle.*
import kotlin.reflect.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


public interface RaptorEventSource {

	public fun asFlow(): Flow<RaptorEvent>
}


@Suppress("UNCHECKED_CAST")
public suspend fun <Event : RaptorEvent> RaptorEventSource.subscribeIn(
	scope: CoroutineScope,
	event: KClass<out Event>,
	action: suspend (Event) -> Unit,
): Job =
	asFlow()
		.filter(event::isInstance)
		.let { it as Flow<Event> }
		.startIn(scope, action)


public suspend inline fun <reified Event : RaptorEvent> RaptorEventSource.subscribeIn(
	scope: CoroutineScope,
	noinline action: suspend (Event) -> Unit,
): Job =
	subscribeIn(scope, event = Event::class, action = action)
