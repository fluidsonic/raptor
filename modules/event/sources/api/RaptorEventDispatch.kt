package io.fluidsonic.raptor.event

import kotlin.coroutines.*
import kotlinx.coroutines.*


/**
 * Coroutine context element marking code that runs as part of delivering events to a subscriber whose emitter waits
 * for it.
 *
 * It's present in the collection coroutine of every subscription made with [RaptorEventSource]'s `subscribeIn`, so
 * it's visible via `currentCoroutineContext()[RaptorEventDispatch]` while a subscriber's handler runs. `raptor-domain`
 * exposes the same element as `RaptorAggregateStreamDispatch` and adds it to aggregate and projection stream
 * subscriptions, whose handlers hold up aggregate commits.
 *
 * Coroutines launched from within the handler's own context inherit it, including those started by flow operators
 * like `buffer` in a flow the handler collects. Coroutines launched in other scopes (e.g. `otherScope.launch { … }`),
 * code consuming a `Channel` fed by the handler, and code collecting [RaptorEventSource.asFlow] directly don't.
 *
 * Events are delivered unbuffered: no further event can be emitted until every subscriber has finished handling the
 * previous one. While this marker is present, the handler therefore holds up the next emission.
 *
 * Code that may block for a long time (e.g. retrying I/O) can use this marker to fail fast instead.
 */
public object RaptorEventDispatch : CoroutineContext.Element, CoroutineContext.Key<RaptorEventDispatch> {

	override val key: CoroutineContext.Key<*>
		get() = this


	override fun toString(): String =
		"RaptorEventDispatch"
}


/**
 * Whether this context belongs to code processing an event whose emitter waits for it, i.e. whether it contains
 * [RaptorEventDispatch].
 */
public val CoroutineContext.isProcessingRaptorEvent: Boolean
	get() = this[RaptorEventDispatch] != null


/**
 * Whether the current coroutine is processing an event or aggregate stream message whose emitter waits for it, i.e.
 * whether its context contains [RaptorEventDispatch]. See [RaptorEventDispatch] for which code is and isn't covered.
 */
public suspend fun isProcessingRaptorEvent(): Boolean =
	currentCoroutineContext().isProcessingRaptorEvent
