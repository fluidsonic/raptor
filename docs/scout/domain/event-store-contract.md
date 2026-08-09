# Aggregate event store: ordering contract and operational limits

Load-bearing invariants an aggregate store implementation must satisfy, plus operational
hazards documented only in code comments.

- **Event IDs are globally monotonic and double as Mongo `_id`.** `RaptorAggregateEventId`
  (a Long) persists as the document `_id`; both Mongo stores order loads by `_id`, so IDs must
  be monotonic across *all* aggregates, not just per-aggregate. `DefaultAggregateManager.start()`
  asserts every loaded event's id equals `lastEventId + 1` (contiguous from 1). `loadAggregate`
  (single-aggregate reads) instead orders by `version` — see `aggregate-loader-interface.md`.
- **No startup batching by `lastVersionInBatch` anymore.** `RaptorAggregateEvent.lastVersionInBatch`
  (required `>= version`, carried through unchanged to `RaptorAggregateProjectionEvent`) is still on
  the wire model, but `DefaultAggregateManager.start()` no longer buffers events per aggregate or
  aborts startup on an incomplete batch — it dispatches each loaded event immediately via `process()`
  as the stream delivers it. `AggregateState.addEvent` still enforces contiguous per-aggregate
  versions (no gaps).
- **Persisted BSON field order is a hard decode contract** — see `event-bson-field-order.md`.
- **Single-instance only; write conflict is unrecoverable.** `MongoAggregateStore` doesn't
  support horizontal scaling (TODO); its `add()` rethrows `MongoBulkWriteException` with a
  comment that Raptor "cannot recover from this without stopping Raptor & starting a new one."
  Duplicate-key protection is a unique index on `(aggregateType, aggregateId, version)`.
  **A multi-instance deploy violates the design.**
- **`reload()` diverges by backend:** `MemoryIndividualAggregateStore` clears its cache and
  returns `emptyList()`; `MongoIndividualAggregateStore` reloads and returns all events. Same
  interface method, opposite contract.

Related: `domain/command-execution.md`, `domain/individual-aggregates.md`,
`domain/aggregate-loader-interface.md`, `domain/event-bson-field-order.md`.
