# Resolved hook filters: index-aligned list, never a map keyed by hook

Matters if you touch `HookDispatcher`'s internal storage of resolved ID-class filters, or add any other
per-hook cache keyed by the hook instance itself.

`modules/domain/sources/implementation/HookDispatcher.kt` stores `resolvedAggregateIdClassFilters` /
`resolvedProjectionIdClassFilters` as `List<Set<...>?>`, index-aligned with the `hooks` list and looked
up via `hooks.indices` — deliberately not a `Map<RaptorDomainStreamHook, Set<...>?>` keyed by the hook
object. `RaptorDomainStreamHook` (`modules/domain/sources/api/RaptorDomainStreamHook.kt`) is a plain
interface with no `equals()`/`hashCode()` contract, so an implementation is free to override structural
equality (e.g. two hook instances considered "equal" because they share some label or configuration
value). Keying a map by such a hook would collapse those two distinct instances into one entry, silently
applying one hook's resolved filter to both — each hook still appears once in `hooks` and gets dispatched
to independently, but they'd share whichever filter happened to win the `associateWith`/`getOrPut` race.

This was an actual bug (map-keyed-by-hook via `hooks.associateWith(...)`), fixed by switching to the
index-aligned list. The regression test is
`HookFilteringTests.testHooksWithCollidingEqualityAreDispatchedIndependently`, using fixture `EqualityHook`
(overrides `equals()`/`hashCode()` by a shared label) to prove two such hooks are dispatched independently.
Any future refactor back toward a map keyed by the hook itself would reintroduce this silently.

Related: `hook-id-class-filtering.md`.
