package io.fluidsonic.raptor.event

import kotlin.coroutines.*
import kotlinx.coroutines.*


/**
 * Coroutine context element marking code that runs as part of delivering an event to a synchronous subscriber.
 *
 * It's present in the coroutine that runs the handler of every subscription made with `async = false`, so it's
 * visible via `currentCoroutineContext()[RaptorEventDispatch]` while such a handler runs. The emitter waits for
 * these handlers to finish, so a slow handler holds up the emitter (e.g. an aggregate commit).
 * Coroutines launched from within the handler's own context inherit it. Coroutines launched in other scopes
 * (e.g. `otherScope.launch { … }`) or code consuming a `Channel` fed by the handler don't.
 *
 * Handlers of subscriptions made with `async = true` don't get this marker added (they only inherit it if the scope
 * they were subscribed in already carries it). They are started undispatched, so the part of such a handler before
 * its first suspension still runs within the emitter and holds it up, unmarked.
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
 * Whether the current coroutine is processing an event whose emitter waits for it, i.e. whether its context contains
 * [RaptorEventDispatch]. See [RaptorEventDispatch] for which code is and isn't covered.
 */
public suspend fun isProcessingRaptorEvent(): Boolean =
	currentCoroutineContext().isProcessingRaptorEvent
