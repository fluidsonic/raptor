# Aggregate store implementations have no dedicated test source sets

`domain-mongo` and `domain-memory` (the two `RaptorAggregateStore` implementations) have no
`tests`/`tests-jvm` source set at all today. Their `loadAggregate`/`load`/`add` behavior is
validated only indirectly, through `TestAggregateStore` — an intentionally byte-identical
stand-in kept in `modules/domain`'s own test source set
(`modules/domain/tests/utility/TestAggregateStore.kt`), which has no build dependency on either
implementation module. A change to `MemoryAggregateStore` or `MongoAggregateStore` must be
mirrored by hand into `TestAggregateStore` for the test suite to still exercise it.

No Mongo integration-test infrastructure exists for aggregate stores specifically:
`modules/store-mongo/tests-jvm/utility/` has in-memory Mongo fakes (`TestMongoDatabase`,
`TestMongoCollection`), but they are `internal` to `store-mongo`'s own test source set and
unusable from `domain-mongo`.

**`MemoryAggregateStore` and `TestAggregateStore` both index events by aggregate ID** for
`loadAggregate` (a `MutableMap<RaptorAggregateId, MutableList<...>>` alongside the flat
`events` list), instead of scanning every event on each call. `add()` is the only place that
appends to both structures — any future bypass of `add()` must update the index too, or
`load()` and `loadAggregate()` will silently diverge.

Related: `aggregate-loader-interface.md`.
