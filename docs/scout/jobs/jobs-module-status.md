# jobs module is contract-only — no scheduler is provided

The `jobs` module defines the scheduling contract but ships **no scheduler implementation**;
scheduling fails at runtime unless another module provides one.

- **No `RaptorJobScheduler` is ever provided into DI.** The module defines the
  `RaptorJobScheduler` interface and the `RaptorScope.jobScheduler` accessor (which is just
  `di.get()`), but nowhere calls `di.provide<RaptorJobScheduler>`. `RaptorJobsPlugin.complete`
  provides only `RaptorJobRegistry`. So installing the jobs plugin gives you executor
  registration + a registry, but `jobScheduler` resolution throws unless some other
  module/plugin provides a scheduler. Anchors:
  `modules/jobs/sources-jvm/scheduling/RaptorJobScheduler.kt` (`jobScheduler`),
  `assembly/RaptorJobsPlugin.kt`.
- **Duplicate executor IDs: strict at registration, silent in the registry.**
  `RaptorJobsComponent.register` `check()`s on a duplicate `group.id`, but
  `RaptorJobRegistryImpl` rebuilds its map with `associateBy` (last wins on duplicates). The
  public `RaptorJobRegistry(executors)` constructor has no guard, so callers building a
  registry directly can silently lose colliding executors.
- **The DI-dependency executor DSL unchecked-casts.** `register(group, dependencyType,
  execute)` resolves the dependency at execution time via `di.get(dependencyType) as
  Dependency` — a mismatched `KType` fails with `ClassCastException` at job-execution time, not
  at registration.
- **`RaptorJobTiming` mixes three time libraries:** `AtInterval` uses `kotlin.time.Duration`
  (Kotlin standard library), `AtDateTime`/`DailyAtTime` use `kotlinx.datetime`
  (`LocalDateTime`/`LocalTime`/`TimeZone`), and `RaptorJobStatus` uses fluid-time's `Timestamp`.
  Only **fluid-time** is a direct dependency (an `api` entry in `modules/jobs/build.gradle.kts`);
  **`kotlinx.datetime`** is an external dependency pulled in only transitively (not declared),
  while `kotlin.time` needs no dependency at all — it ships with the Kotlin standard library.
