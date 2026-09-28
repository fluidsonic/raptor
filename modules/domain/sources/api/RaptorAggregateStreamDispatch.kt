package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.event.*


/**
 * Coroutine context element marking code that runs as part of delivering aggregate events to a synchronous subscriber.
 *
 * It's present while the handler of a [RaptorAggregateEventSource] or [RaptorAggregateProjectionEventSource]
 * subscription made with `async = false` runs, visible via `currentCoroutineContext()[RaptorAggregateStreamDispatch]`.
 * Live events are delivered while an aggregate commit holds the app-wide commit lock, so a slow handler holds up
 * every commit. Replayed events are delivered during start, which commits wait for, and
 * [RaptorAggregateReplayCompletedEvent] is delivered while the commit lock is held.
 *
 * Handlers of subscriptions made with `async = true` don't get this marker added (they only inherit it if the scope
 * they were subscribed in already carries it). Their part before the first suspension still runs within the commit
 * and holds it up, unmarked.
 * [RaptorDomainStreamHook] callbacks are not suspending, so this marker can't reach them.
 *
 * See [RaptorEventDispatch] for inheritance by child coroutines.
 */
public typealias RaptorAggregateStreamDispatch = RaptorEventDispatch
