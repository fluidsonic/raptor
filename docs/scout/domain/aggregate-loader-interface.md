# `RaptorAggregateLoader`: per-aggregate reads and why `definition` is caller-supplied

`RaptorAggregateStore`'s read-only members (`lastEventTimestampOrNull`, `load`) were extracted
into a new `RaptorAggregateLoader` interface, which `RaptorAggregateStore` now extends. Anchor:
`modules/domain/sources/api/RaptorAggregateLoader.kt`.

- **`loadAggregate(definition, id, afterVersion)` orders by `version`, not `_id`.** Unlike
  `load()` (global, `_id`-ordered), this streams one aggregate's history ascending by
  `version` — the same order as event-id order for a single aggregate, since per-aggregate
  versions are contiguous. `afterVersion` is exclusive and resumable; `null` returns full
  history. An unknown `id` (or none past `afterVersion`) yields an empty flow, not a throw.
- **Individual aggregates are explicitly rejected**, not silently emptied:
  `check(!definition.isIndividual)` throws `IllegalStateException` (see
  `individual-aggregates.md`).
- **`id` must be exactly `definition.idClass`**, checked via `definition.idClass == id::class`
  (not `isInstance`, which would also accept a subclass) — throws `IllegalArgumentException`
  otherwise.
- **Why the caller supplies `definition`:** `RaptorAggregateEvent` itself carries no
  `aggregateType` field (it's a BSON-only discriminator written from `definition.discriminator`,
  `RaptorAggregateEventBson.kt`), and `MongoAggregateStore` is constructed via
  `RaptorAggregateStore.Companion.mongo(...)` before aggregate definitions complete — it has no
  other way to resolve an id class to a discriminator.
- **No extra casts needed at call sites:** `RaptorAggregateDefinitions.get(id)`/`get(idClass)`
  already return an out-projected, `Id`-parameterized definition via one internal
  `@Suppress("UNCHECKED_CAST")`, so a generic `loadAggregate<Id>` accepts it directly.

Related: `event-store-contract.md`, `individual-aggregates.md`, `aggregate-store-test-coverage.md`.
