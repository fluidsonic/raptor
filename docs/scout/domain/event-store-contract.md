# Aggregate event store: ordering contract and operational limits

Load-bearing invariants an aggregate store implementation must satisfy, plus operational
hazards documented only in code comments.

- **Event IDs are globally monotonic and double as Mongo `_id`.** `RaptorAggregateEventId`
  (a Long) persists as the document `_id`; both Mongo stores order loads by `_id`, so IDs must
  be monotonic across *all* aggregates, not just per-aggregate. `DefaultAggregateManager.start()`
  asserts every loaded event's id equals `lastEventId + 1` (contiguous from 1).
- **Batch closure via `lastVersionInBatch`.** Events for different aggregates interleave in the
  stream; each carries `lastVersionInBatch` so the manager knows when a per-aggregate batch is
  complete (`event.version == event.lastVersionInBatch`). An aggregate with unclosed batch events
  after the stream ends aborts startup. `AggregateState.addEvent` also enforces contiguous
  per-aggregate versions (no gaps).
- **Persisted BSON field order is a hard decode contract.** `RaptorAggregateEventBson` decode is
  stateful: `aggregateType` must precede `aggregateId`/`changeType`; `changeType` must precede
  `change`. A reordered encoder or manual DB edit **fails explicitly** with an "Invalid field
  order" error rather than decoding wrong data silently. Anchor:
  `modules/domain-mongo/sources/assembly/RaptorAggregateEventBson.kt`.
- **Single-instance only; write conflict is unrecoverable.** `MongoAggregateStore` doesn't
  support horizontal scaling (TODO); its `add()` rethrows `MongoBulkWriteException` with a
  comment that Raptor "cannot recover from this without stopping Raptor & starting a new one."
  Duplicate-key protection is a unique index on `(aggregateType, aggregateId, version)`.
  **A multi-instance deploy violates the design.**
- **`reload()` diverges by backend:** `MemoryIndividualAggregateStore` clears its cache and
  returns `emptyList()`; `MongoIndividualAggregateStore` reloads and returns all events. Same
  interface method, opposite contract.

Related: `domain/command-execution.md`, `domain/individual-aggregates.md`.
