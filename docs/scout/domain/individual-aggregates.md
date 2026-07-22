# Individual aggregates are a separate, restricted path

Aggregates registered with `individual = true` behave very differently from regular
aggregates; the divergence is scattered across guards and the completion block.

- **No projectors, no command execution.** `_project()` throws "Can't register a projector
  for an individual aggregate"; `Commit.add` throws "Can't use command execution for
  individual aggregates."
- **Own store per discriminator.** `RaptorAggregatesComponent.completeIn` builds, per
  individual definition, a `DefaultIndividualAggregateManager` plus a
  `RaptorIndividualAggregateStore` (via runtime `createType`), backed by
  `RaptorIndividualAggregateStoreFactory.create` with store name **`"events_<discriminator>"`**
  — the naming convention lives only here. Anchor:
  `modules/domain/sources/assembly/RaptorAggregatesComponent.kt` (`completeIn`).
- **`DefaultIndividualAggregateManager.commit` reads `lastVersion` from the store each time**
  (not from in-memory state) and does not support batching on reload.
- **Not concurrent.** `RaptorIndividualAggregateStore` carries a class KDoc "Doesn't support
  concurrent access." Callers must serialize; concurrency is expected from a higher layer
  (`RaptorIndividualAggregateManager` mediating `commit` with `expectedVersion` optimistic
  concurrency).
- **Generics are erased and unchecked-cast.** `.memory()`/`.mongo()` construct the store with
  erased `RaptorAggregateId`/`RaptorAggregateChange<...>` and the reified overloads
  `@Suppress("UNCHECKED_CAST")` cast to `<Id, Change>`. The store does no runtime type
  discrimination; use the reified `create<Id, Change>(name)` helper, not the raw `KType`
  overload. The Mongo variant validates at init that `eventType.classifier ==
  RaptorAggregateEvent::class`.

`RaptorAggregateProvider.provide(id)` returns `Pair<RaptorAggregate<...>, Int>` where the
bare `Int` is the aggregate's current version — the same value used as `expectedVersion` in
`commit`. Related: `domain/event-store-contract.md`.
</content>
