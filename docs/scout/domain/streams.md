# Aggregate/projection stream delivery semantics

Which events a subscriber actually receives depends heavily on *which* subscribe function
it picks — a frequent source of "my handler never fires" bugs.

- **Cold replay is one bulk message; live changes are per-batch.** `start()` wraps all loaded
  batches in a single `RaptorAggregateStreamMessage.Replay(batches)`, emitted once, followed by a
  `Loaded` marker. Live commits emit each `RaptorAggregateEventBatch` individually via `process()`.
  Anchor: `modules/domain/sources/implementation/DefaultAggregateManager.kt` (`start`, `process`).
- **`events()` / `subscribeIn` drop replay and pre-`Loaded` messages.** The `events()` extension
  (on both streams) maps `Replay` to `emptyFlow()`; `subscribeIn` does `dropWhile { it !is Loaded }`.
  Only the projection stream's `subscribeMessagesIn` flattens `Replay`
  (`flatMapConcat { message.batches.asFlow() }`) without gating on `Loaded` — the aggregate stream
  has no such helper, so seeing its full history means consuming `messages` directly. Anchors:
  `modules/domain/sources/api/RaptorAggregateStream.kt`, `RaptorAggregateProjectionStream.kt`
  (`subscribeMessagesIn`).
- **`errorStrategy` is accepted but ignored; failures blacklist the id instead.** Every
  `subscribeIn` overload takes `errorStrategy: RaptorAggregateStream.ErrorStrategy = skip`, but
  the core overloads (marked `// FIXME use`) never read it — handling is hard-coded to
  skip-the-failing-id: on a non-cancellation throw, `subscribeIn` adds the event's id to a
  lazily-created failed-id set (later events for that id are dropped silently) and re-throws via
  `scope.launch { throw e }`, surfacing as an uncaught exception on the scope rather than to the
  caller. `CancellationException` is rethrown directly.
- **Subscription returns only after collection has started**, via
  `.onStart { completion.complete(Unit) }.launchIn(scope).also { completion.await() }` — an
  onStart+await barrier against lost events.
- **Batch `subscribeIn` preserves original batch objects on the all-match path** (returns
  `batch` unchanged when every event matches `changeClass`), only `batch.copy(events=filtered)`
  on partial match, and drops (returns null) when filtered empty.

The stop/flush barrier (`DefaultAggregateStream`) uses a self-emitted `Ping`/`stopMessage`
round-trip through the shared flow, hidden from consumers via `filterNot`.

Related: `../event/dispatch-marker.md` (coroutine context marker present in these collectors).
