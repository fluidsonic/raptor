# `RaptorEventDispatch`: marking handlers that hold up the emitter

A `CoroutineContext.Element`/`Key` object (`modules/event/sources/api/RaptorEventDispatch.kt`) a subscriber handler can check, via `isProcessingRaptorEvent()`, to tell whether its slowness currently blocks the emitter.

- **Only `async = false` subscriptions get it.** `Subscription.handle` in `modules/event/sources/implementation/ParallelEventProcessor.kt` launches the handler with `RaptorEventDispatch` as context when `async` is false, and `process()` joins that job, so the emitter waits. With `async = true` the context is empty and no job is joined.
- **Both modes start `UNDISPATCHED`.** An async handler's code before its first suspension therefore still runs inside the emitter, holding it up, and is unmarked. The marker cannot detect that part.
- **Inheritance.** Children launched in the handler's own context inherit it (including flow operators such as `buffer()`). Other scopes, or a `Channel` consumer fed by the handler, do not. An async handler inherits it only if the subscribing scope already carried it.
- **The domain module repeats the pattern.** `RaptorAggregateStreamDispatch` (`modules/domain/sources/api/RaptorAggregateStreamDispatch.kt`) is a `typealias` for the same object. `DefaultAggregateEventProcessor` and `DefaultAggregateProjectionEventProcessor` each have their own `Subscription.handle` with the same `async` switch. Live events are delivered while a commit holds the app-wide commit lock, so a slow synchronous handler holds up every commit. Replayed events are delivered during start, which commits wait for.
- **Hooks can't observe it.** `RaptorDomainStreamHook` callbacks are not suspending.
- Tests: `modules/event/tests/ParallelEventProcessorDispatchTests.kt`, `modules/domain/tests/StreamDispatchTests.kt`.

Related: `event-module-status.md`.
