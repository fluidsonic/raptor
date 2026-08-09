# Hook ID-class filtering: resolved once, not re-checked by is-a

`RaptorDomainStreamHook.aggregateIdClassFilter` / `projectionIdClassFilter` let a hook opt into a subset
of aggregate/projection ID classes. The is-a matching described in the property KDoc
(`modules/domain/sources/api/RaptorDomainStreamHook.kt`) only happens once.

- **Resolved once at construction, then exact-match per event at dispatch.** All of this lives in
  `modules/domain/sources/implementation/HookDispatcher.kt`, an internal `HookDispatcher` class that
  `DefaultAggregateManager` constructs and holds (`hookDispatcher`) — `resolveAggregateIdClassFilter`/
  `resolveProjectionIdClassFilter` expand each hook's declared classes into the concrete *registered*
  ID classes they match via `isSuperclassOf`, stored index-aligned with `hooks` (see
  `hook-filter-storage-identity.md` for why it's a list, not a map keyed by hook).
  `dispatchAggregateEvent`/`dispatchAggregateProjectionEvent` then just do a `Set.contains` against the
  event's exact `aggregateId::class`/`projectionId::class` — no reflection on the hot path.
- **One event at a time, live or replay alike.** `process()` in `DefaultAggregateManager` calls
  `dispatchAggregateEvent`/`dispatchAggregateProjectionEvent` for every event, whether it arrives during
  the initial `store.load()` replay or from a later live commit — no separate replay-batch path exists.
- **No filters anywhere = zero behavioral change.** `hasAggregateFilter`/`hasProjectionFilter` gate an
  unfiltered fast path that calls every hook directly, with no per-hook `Set` lookup at all.
- **A filter class matching only individual-aggregate ID classes fails fast at construction** with a
  distinct error (aggregate filter only, not projection) — individual aggregates never dispatch to hooks
  at all (see `individual-aggregates.md`).
- **`dispatchReplayCompleted` calls every hook's `onReplayCompleted` unconditionally** — neither filter
  gates this method; a hook filtered to `emptySet()` still gets exactly one `onReplayCompleted` call.

Related: `individual-aggregates.md`, `hook-filter-storage-identity.md`.
