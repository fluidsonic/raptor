# Aggregate event store: ordering contract and operational limits

Load-bearing invariants an aggregate store implementation must satisfy, plus operational
hazards documented only in code comments.

- **Event IDs are globally monotonic and double as Mongo `_id`.**
  `RaptorAggregateEventId` (a Long) persists as the document `_id` (`Fields.id = "_id"`).
  Both Mongo stores order by `_id` for loads, `_id` descending to find the newest, and
  `MongoIndividualAggregateStore.lastEventId` reads `_id` directly. So the event id is both
  primary key and global ordering key — IDs must be globally monotonic across all
  aggregates. `DefaultAggregateManager.start()` asserts every loaded event's id equals
  `lastEventId + 1` (contiguous from 1).
- **Batch closure via `lastVersionInBatch`.** Events for different aggregates interleave in
  the stream; each event carries `lastVersionInBatch` so the manager knows when a
  per-aggregate batch is complete (`event.version == event.lastVersionInBatch`). Any
  aggregate with unclosed batch events after the stream ends aborts startup.
  `AggregateState.addEvent` also enforces contiguous per-aggregate versions (each event's
  version must equal the previous + 1; no gaps).
- **Persisted BSON field order is a hard decode contract.** `RaptorAggregateEventBson`
  decode is stateful: `aggregateType` must precede `aggregateId` and `changeType`;
  `changeType` must precede `change`. Violations throw "Invalid field order when
  decoding ...". A manual DB edit or reordered encoder that changes this order makes decoding
  **fail explicitly** with that "Invalid field order" error (thrown by `checkNotNull` on the
  not-yet-resolved `definition`/`changeDefinition`) — it does not decode wrong data silently.
  Anchor: `modules/domain-mongo/sources/assembly/RaptorAggregateEventBson.kt`.
- **Single-instance only; write conflict is unrecoverable.** `MongoAggregateStore` does not
  support horizontal scaling (TODO); its `add()` rethrows `MongoBulkWriteException` with a
  comment that Raptor "cannot recover from this without stopping Raptor & starting a new
  one." Version-conflict detection is commented out. Duplicate-key protection is a unique
  index on `(aggregateType, aggregateId, version)` created in `start()`. **A multi-instance
  deploy violates the design.**
- **`load()` sets `batchSize(1_000_000)` deliberately** — a comment cites a measured "4-6x
  speed increase" for full event-stream reads. Not a bug.
- **`reload()` diverges by backend:** `MemoryIndividualAggregateStore.reload()` clears the
  cache and returns `emptyList()`; `MongoIndividualAggregateStore.reload()` reloads all
  events and returns them. Same interface method, opposite contract.

Related: `domain/streams.md`, `domain/command-execution.md`,
`domain/individual-aggregates.md`.
