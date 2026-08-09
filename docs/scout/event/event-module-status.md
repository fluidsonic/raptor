# event module is a wired-but-provisional stub

The `event` module looks fully assembled (emitter + source + DI wiring) but its core behavior
is unfinished — know this before relying on it.

- **No shared-flow dispatch; exact-class-keyed subscriptions instead.** `ParallelEventProcessor`
  (implements both `RaptorEventProcessor` and `RaptorEventSource`) stores handlers in a
  `Map<KClass<out RaptorEvent>, List<Subscription<*>>>` keyed by the event's exact runtime class
  — no `isInstance`/supertype matching, and no `MutableSharedFlow` in between. `process()` looks
  up only the exactly-matching subscriptions and awaits each handler job before returning. Anchor:
  `modules/event/sources/implementation/ParallelEventProcessor.kt`.
- **No error handling anywhere.** There is no `onError` parameter or callback at all —
  `RaptorEventPlugin.complete()` constructs a bare `ParallelEventProcessor()`. A subscriber
  handler's exception (thrown inside the `CoroutineStart.UNDISPATCHED` `scope.launch`) is neither
  caught nor reported by the processor; only the enclosing coroutine scope's own exception
  handling sees it. Lifecycle wind-up/down of event handling is still a TODO
  (`RaptorEventPlugin.install()` comment).
- **`Flow.startIn` (`modules/event/sources/utility/Flow.kt`) is now dead code** — nothing in the
  event module calls it since dispatch moved off `Flow` entirely; it survived the refactor to
  `ParallelEventProcessor` unnoticed.
- The plugin registers `RaptorEventEmitter`/`RaptorEventSource` into DI only when the optional
  `RaptorDIPlugin` is present.
