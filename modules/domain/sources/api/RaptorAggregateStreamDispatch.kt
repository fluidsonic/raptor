package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.event.*


/**
 * Coroutine context element marking code that runs as part of delivering aggregate stream messages to a subscriber.
 * It's the same element as [RaptorEventDispatch], so checking either key covers both aggregate streams and events.
 *
 * It's present in the collection coroutine of every subscription made with [RaptorAggregateStream]'s and
 * [RaptorAggregateProjectionStream]'s `subscribeIn` or [RaptorAggregateProjectionStream]'s `subscribeMessagesIn`,
 * so it's visible via `currentCoroutineContext()[RaptorAggregateStreamDispatch]` while a subscriber's handler runs.
 * Coroutines launched from within the handler's own context inherit it, including those started by flow operators
 * like `buffer` in a flow the handler collects. Coroutines launched in other scopes (e.g. `otherScope.launch { … }`),
 * code consuming a `Channel` fed by the handler, and code collecting [RaptorAggregateStream.messages] or
 * [RaptorAggregateProjectionStream.messages] directly don't.
 *
 * The streams are unbuffered: no further message can be emitted until every subscriber has finished handling the
 * previous one. While this marker is present, the handler therefore holds up the next emission:
 * - Live event batches are emitted while an aggregate commit holds the app-wide commit lock, so a slow handler holds
 *   up the next commit and, through the lock, every commit after it.
 * - [RaptorAggregateStreamMessage.Replay] and [RaptorAggregateProjectionStreamMessage.Replay] are emitted during
 *   start outside of the commit lock, and [RaptorAggregateStreamMessage.Loaded] and
 *   [RaptorAggregateProjectionStreamMessage.Loaded] are emitted while it's held. Start (and thus every commit)
 *   doesn't proceed until they've been taken by all subscribers.
 *
 * Code that may block for a long time (e.g. retrying I/O) can use this marker to fail fast instead.
 *
 * [RaptorDomainStreamHook] callbacks are not suspending and run synchronously within commits and start.
 * No coroutine context is observable from them, so this marker can't reach them.
 */
public typealias RaptorAggregateStreamDispatch = RaptorEventDispatch
