# Projections: eligibility, replay gating, and the DI wiring

How projections are selected and loaded — keyed off the aggregate ID type, not explicit
registration.

- **Eligibility is decided by the aggregate ID type.**
  `DefaultAggregateProjectionLoaderManager.addEvent` does `event.aggregateId as?
  RaptorAggregateProjectionId ?: return null`, then routes by `id::class`. Loaders and
  projector factories are keyed by the **ID class**, not the projection class, so two
  distinct projections sharing one ID class collide on a single loader (there are FIXMEs
  about this). Anchor:
  `modules/domain/sources/implementation/DefaultAggregateProjectionLoaderManager.kt`.
- **Projection queries are forbidden during replay.**
  `DefaultAggregateProjectionLoader.fetchAll()` / `fetchOrNull()` do
  `check(loaded.isCompleted)` and fail with "Cannot fetch projections during replay. Use
  event.projection for point-in-time state." `loaded` is a single `CompletableDeferred`
  shared across all loaders, completed only by `noteLoaded()` after the whole cold replay
  finishes. During startup, read the projection carried on each event instead.
- **Registering a projector provides a reflection-typed DI binding.**
  `RaptorAggregateComponent._project()` (@RaptorInternalApi) reaches into the captured
  `topLevelScope` and, under `optional(RaptorDIPlugin)`, calls `di.provide` with a `KType`
  built at runtime via `RaptorAggregateProjectionLoader::class.createType(...)` from the
  projection and ID classes (known only as `KClass` at the binding site). This makes
  `di.get<RaptorAggregateProjectionLoader<Projection, Id>>()` resolvable for DI consumers;
  the binding's own factory delegates back to `context.projectionLoader(idClass)`. Note the
  `RaptorScope.projectionLoader<Projection, Id>()` extension does **not** use this binding — it
  resolves `RaptorAggregateProjectionLoaderManager` from DI and calls `getOrCreate(idClass)`
  (`modules/domain/sources/api/RaptorScope.kt`). Anchor:
  `modules/domain/sources/assembly/RaptorAggregateComponent.kt` (`_project`).
- **A projection event can represent a deletion.** `RaptorAggregateProjectionEvent` requires
  at least one of `projection`/`previousProjection` non-null and its `projectionId` falls
  back `projection?.id ?: previousProjection?.id`, so a null `projection` (a delete) still has
  a stable id.

Note the fold-implementation divergence for deletes: an aggregate's `handle(Deleted)` may
retain state (e.g. flips a flag), while a projector's `apply(Deleted)` drops the projection
to null. Do not assume aggregate and projector agree post-delete.
</content>
