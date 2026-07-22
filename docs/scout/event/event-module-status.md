# event module is a wired-but-provisional stub

The `event` module looks fully assembled (emitter + source + DI wiring) but its core
behavior is unfinished — know this before relying on it.

- **Error handling is a no-op.** `RaptorEventPlugin.complete()` constructs
  `ParallelDispatchEventProcessor` with an empty `onError` lambda (`{ _, _ -> }`); the
  processor's `onError` is marked `FIXME Actually use`. Errors during event processing are
  currently discarded. Lifecycle wind-up/down of event handling is a TODO. Anchors:
  `modules/event/sources/assembly/RaptorEventPlugin.kt`,
  `modules/event/sources/implementation/ParallelDispatchEventProcessor.kt`.
- **Dispatch drops events when there is no collector.** `process()` emits into a plain
  `MutableSharedFlow<RaptorEvent>()` with defaults (`replay = 0`, `extraBufferCapacity = 0`),
  so `emit()` delivers only to currently-subscribed collectors and drops the event when there
  are none; late subscribers never see past events. A `Flow.startIn` helper launches
  collection with `CoroutineStart.UNDISPATCHED` so a subscriber registers synchronously before
  `emit` can race past it. A FIXME even questions whether parallel collection is desired.
- **Typed subscription re-filters the single shared flow.** `RaptorEventSource.subscribeIn`
  does `asFlow().filter(event::isInstance)` then an unchecked cast to `Flow<Event>` — there is
  no per-type multiplexing; every subscriber re-filters the same source flow.
- The plugin registers `RaptorEventEmitter`/`RaptorEventSource` into DI only when the optional
  `RaptorDIPlugin` is present.
</content>
