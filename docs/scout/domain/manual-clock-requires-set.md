# `ManualClock` in domain tests needs `.set()` before any live command

`io.fluidsonic.time.ManualClock` (fluid-time, version: 0.19.0 as of this pass) throws
`IllegalStateException: ManualClock was used without specifying a timestamp.` from `.now()` until
`.set(Timestamp)` has been called at least once.

This bites indirectly: `DefaultAggregateManager.commit()` calls `clock.now()` to timestamp every
event, so *any* test that executes a live command against a `RaptorAggregateCommandExecutor` — not
only a test that reads the clock directly — needs the clock seeded first. Existing domain tests
construct `ManualClock()` with no timestamp, then call `clock.set(Timestamp...)` right before the
first live command (`modules/domain/tests/EventTests.kt`, `AssemblyTests.kt`, `ExecutionTests.kt`);
`HookFilteringTests.kt` instead seeds it inline at construction
(`ManualClock().also { it.set(Timestamp.fromEpochSeconds(0)) }`). A new domain test that constructs
`ManualClock()` and executes a live command without calling `.set()` first anywhere on that path
fails with the above exception, which gives no hint that the fix is seeding a timestamp.

Related: `command-execution.md`.
