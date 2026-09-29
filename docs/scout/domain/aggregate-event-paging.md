# `loadPage` and `loadAggregateEvents`: paged, filtered event queries

Global event-ID-ordered paging over non-individual aggregates. Anchors:
`RaptorAggregateLoader.loadPage` in `modules/domain/sources/api/RaptorAggregateLoader.kt` and
`loadAggregateEvents` in `modules/domain/sources/api/RaptorScope.kt`.

- **`loadPage` is abstract, with no default body.** Every `RaptorAggregateStore` must implement it,
  and it returns a non-`suspend` `Flow` whose arguments are validated eagerly, at the call rather
  than at collection. `MAX_PAGE_SIZE` bounds `limit`.
- **`loadAggregateEvents` takes KClasses and resolves them** through `RaptorAggregateDefinitions`.
  An unregistered ID class or change class throws `IllegalArgumentException`; an individual
  aggregate throws `IllegalStateException`.
- **Filters always pair `aggregateType` with `changeType`**, because change discriminators are
  unique only within one aggregate.
- **Change classes match exactly.** A nested sealed leaf must be listed itself; listing its parent
  does not include it.
- **An empty `changes` map returns nothing**, while a `null` map disables filtering.
  `MongoAggregateStore.loadPage` short-circuits because an empty `$or` is invalid in MongoDB.
- **Mongo expands "all changes" (`null`) to the aggregate's registered discriminators** as a
  `changeType $in`. An `aggregateType`-only branch would leave `changeType` unbounded, so the index
  could not return `_id` order and the `$or` branches could not be merge-sorted.
- **Extra Mongo index:** `MongoAggregateStore.start()` also creates `(aggregateType, changeType, _id)`
  for `loadPage`. It is built over the full log on the first start after upgrading; no data migration
  is needed since `changeType` was already stored.
- **Three copies of the body:** the `loadPage` bodies in `MemoryAggregateStore` and
  `TestAggregateStore` must stay identical (see `aggregate-store-test-coverage.md`).
  Tests: `modules/domain/tests/AggregateStoreLoadTests.kt`.

Related: `aggregate-loader-interface.md`, `event-store-contract.md`.
