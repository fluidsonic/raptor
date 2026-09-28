# `RaptorEventDispatch`: detecting handlers that hold up the next emission

A `CoroutineContext.Element`/`Key` object (`modules/event/sources/api/RaptorEventDispatch.kt`) a subscriber
handler can check to tell whether its own slowness is currently blocking delivery.

- **Attached by `RaptorEventSource.subscribeIn`, not by `asFlow()`.** `raptor-event`'s `Flow.kt` `startIn`
  (`scope.launch(RaptorEventDispatch, start = UNDISPATCHED) { collect(action) }`) adds the element to the
  collection coroutine that every `subscribeIn` overload launches. A handler sees it via
  `currentCoroutineContext()[RaptorEventDispatch]`; a coroutine it launches in its own context inherits it —
  including one started by a flow operator like `buffer()` on a flow the handler collects, on both the
  producer and collector side. A coroutine launched in another scope, a `Channel` consumer fed by the handler,
  or code calling `asFlow()` directly never sees it. Covered by `EventDispatchTests`.
- **Unbuffered delivery means the handler blocks the *next* emission, not the current one.**
  `ParallelDispatchEventProcessor.process()` emits into a plain rendezvous `MutableSharedFlow()`, so `emit`
  only returns once every subscriber has taken the value — a slow handler holds up the following `process()`
  call, not the one that triggered it.
- **`raptor-domain` reuses this element as `RaptorAggregateStreamDispatch`**, an `api` dependency on
  `raptor-event` added for exactly this (`modules/domain/build.gradle.kts`,
  `modules/domain/sources/api/RaptorAggregateStreamDispatch.kt` — a `typealias`, not a separate object). Its
  own `Flow.kt` attaches it independently to aggregate/projection stream `subscribeIn`/`subscribeMessagesIn`
  collection coroutines, with the same next-emission-blocks semantics ultimately holding up aggregate commits
  (see `streams.md` in `domain/` for that timing). `RaptorDomainStreamHook` callbacks run synchronously, not
  as coroutines, so they can never observe it either way.

Related: `event-module-status.md`, `../domain/streams.md`.
