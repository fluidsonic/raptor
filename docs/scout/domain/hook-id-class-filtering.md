# Hook ID-class filtering: resolved once, not re-checked by is-a

`RaptorDomainStreamHook.aggregateIdClassFilter` / `projectionIdClassFilter` let a hook opt into a subset
of aggregate/projection ID classes. The is-a matching described in the property KDoc
(`modules/domain/sources/api/RaptorDomainStreamHook.kt`) only happens once.

- **Resolved once at construction, then exact-match at dispatch.** All of this lives in
  `modules/domain/sources/implementation/HookDispatcher.kt`, an internal `HookDispatcher` class that
  `DefaultAggregateManager` constructs and holds (`hookDispatcher`) — `resolveAggregateIdClassFilter`/
  `resolveProjectionIdClassFilter` expand each hook's declared classes into the concrete *registered*
  ID classes they match via `isSuperclassOf`, stored index-aligned with `hooks` (see
  `hook-filter-storage-identity.md` for why it's a list, not a map keyed by hook). `dispatchLive`/`dispatchReplay`
  then just do a `Set.contains` against the event's exact `::class` — no reflection on the hot path.
  `DefaultAggregateManager.process()`/`start()` call `dispatchLive`/`dispatchReplay`/`dispatchLoaded`
  and contain no filtering logic of their own.
- **No filters anywhere = zero behavioral change.** `hasAggregateFilter`/`hasProjectionFilter` gate an
  unfiltered fast path that dispatches the original `batches`/`projectionBatches` list objects to every
  hook, identical to pre-filtering behavior.
- **Filtered cold-replay batches are NOT re-sorted** — `dispatchReplay` groups by index (`groupByIndex`,
  `filterIndexed`) preserving original load order, so a hook filtered to several aggregate ID classes
  sees batches interleaved across types in construction order, not grouped by type or sorted by event
  id. Sorting by `events.first().id` would misorder, since one batch can span non-contiguous event ids.
  Covered by `HookFilteringTests.testMultiClassFilterPreservesGlobalOrder`.
- **A filter class matching only individual-aggregate ID classes fails fast at construction** with a
  distinct error (aggregate filter only, not projection) — individual aggregates never dispatch to hooks
  at all (see `individual-aggregates.md`).
- `dispatchLoaded` sends `Loaded` to every hook outside the filter branches entirely, regardless of filter.

Related: `streams.md`, `individual-aggregates.md`, `hook-filter-storage-identity.md`.
