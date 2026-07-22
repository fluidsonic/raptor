# Aggregate/projection stream delivery semantics

Which events a subscriber actually receives depends heavily on *which* subscribe function
it picks — a frequent source of "my handler never fires" bugs.

- **Cold replay is one bulk message; live changes are per-batch.** `start()` wraps all
  loaded batches in a single `RaptorAggregateStreamMessage.Replay(batches)`, emitted once,
  followed by a `Loaded` marker. Live commits emit each `RaptorAggregateEventBatch`
  individually via `process()`. Anchor:
  `modules/domain/sources/implementation/DefaultAggregateManager.kt` (`start`, `process`).
- **`events()` / `subscribeIn` drop replay and pre-`Loaded` messages.** The `events()`
  extension (present on both streams) maps `Replay` to `emptyFlow()`, and `subscribeIn`
  does `dropWhile { it !is Loaded }`. Only the projection stream's `subscribeMessagesIn`
  flattens `Replay` (`flatMapConcat { message.batches.asFlow() }`) and does not gate on
  `Loaded` — the aggregate stream has no such helper, so seeing its full history means
  consuming `messages` directly. Anchors:
  `modules/domain/sources/api/RaptorAggregateStream.kt`,
  `modules/domain/sources/api/RaptorAggregateProjectionStream.kt` (`subscribeMessagesIn`).
- **`errorStrategy` is accepted everywhere but ignored.** Every `subscribeIn` overload takes
  `errorStrategy: RaptorAggregateStream.ErrorStrategy = skip`; the two core overloads are
  marked `// FIXME use` and never read it (the inline overloads merely forward it) — error
  handling is hard-coded to skip-the-failing-id.
- **A collector failure blacklists that id for the subscription's life.** On a
  non-cancellation throw, `subscribeIn` adds the event's id to a lazily-created failed-id set
  (subsequent events for that id are silently dropped) and re-throws via `scope.launch {
  throw e }` (surfacing through the scope's uncaught-exception path, not to the
  `subscribeIn` caller). `CancellationException` is rethrown directly.
- **Subscription returns only after collection has started.** The suspend `subscribeIn` uses
  `.onStart { completion.complete(Unit) }.launchIn(scope).also { completion.await() }` — an
  onStart+await barrier guaranteeing no lost events between the call returning and collection
  beginning.
- **Batch `subscribeIn` preserves original batch objects on the all-match path** (returns
  `batch` unchanged when every event matches `changeClass`), only `batch.copy(events=filtered)`
  on partial match, and drops (returns null) when filtered empty.

The stop/flush barrier (`DefaultAggregateStream`) uses a self-emitted `Ping`/`stopMessage`
sentinel round-trip through the shared flow, hidden from consumers via `filterNot`.
</content>
