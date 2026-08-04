# `startIn` leaks a job if a start action throws — hangs `runTest`

Matters when writing a test that intentionally makes Raptor startup fail (e.g. testing
manager-construction validation) by calling `raptor.lifecycle.startIn(this)` inside a coroutine test.

`DefaultLifecycle.startIn` (`modules/lifecycle/sources-jvm/implementation/DefaultLifecycle.kt`)
builds `_coroutineContext` with a `SupervisorJob(parent = scope.coroutineContext.job)` *before*
running any start action. Attaching a job with an explicit `parent` registers it as an active child
of that parent immediately — independent of whether it's ever used to launch a coroutine. If a start
action then throws, `startIn` propagates the exception without completing or cancelling that
`SupervisorJob`, and lifecycle state is left stuck at `starting` (`stop()` requires `started`, so it
can't be used to clean up either).

If the test calls `startIn(this)` with `this` being the test's own `TestScope`, the leaked job
becomes a permanently-`Active` child of the test scope's job, and `kotlinx.coroutines.test.runTest`
hangs for its full timeout, failing with `UncompletedCoroutinesError` — with no indication the real
cause is the thrown start action rather than the assertion.

Workaround (see `modules/domain/tests/HookFilteringTests.kt`, `startExpectingFailure`): run
`startIn` in a detached `CoroutineScope(coroutineContext + Job())`, not parented to the test scope,
and `cancel()` it after asserting the failure.

Related: `lifecycle/service-orchestration.md`.
