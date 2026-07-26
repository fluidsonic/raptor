# Lifecycle: action priorities and service orchestration

How the lifecycle plugin brackets service create/start/stop around user actions, and the
concurrency asymmetry between phases.

- **Magic priorities bracket services around user actions.** `RaptorLifecyclePlugin.install`
  registers three actions: `"service creations"` at priority 0, `"services"` (start) at
  `Int.MIN_VALUE + 1`, and `"services"` (stop) at `Int.MAX_VALUE`. `DefaultLifecycle` sorts
  action lists `sortedByDescending { it.priority }` (a *stable* sort), so: services are
  *created* in the same priority-0 tier as user `onStart(priority = 0)` actions, services
  *start* dead last (only `MIN_VALUE` could beat them), and services *stop* first before any
  user `onStop`. Within the priority-0 tier order follows registration order, so service
  creation is **not** guaranteed to run before a user `onStart(priority = 0)` action — a user
  action registered earlier runs first. Services do, however, only actually start after every
  other `onStart` ran (their start action sits at `Int.MIN_VALUE + 1`). Anchors:
  `modules/lifecycle/sources-jvm/assembly/RaptorLifecyclePlugin.kt` (`install`),
  `modules/lifecycle/sources-jvm/implementation/DefaultLifecycle.kt`.
- **Create/stop are concurrent; start is sequential.** `createServices()` launches every
  `controller.createIn()` concurrently in a `coroutineScope`; `stopServices()` stops all
  concurrently in a `supervisorScope`; but `startServices()` calls `controller.start()`
  one-by-one in a plain for-loop (`start()` is non-suspending — it just launches the
  `started()` handler).
- **Service DI providers are only wired when at least one service is registered.**
  `RaptorLifecyclePlugin.complete()` calls `configure(RaptorDIPlugin){...}` only when
  `serviceRegistrations` is non-empty. Separately, `install()` uses `optional(RaptorDIPlugin)`
  to register two convenience providers: a bare `CoroutineContext` resolves to the lifecycle's
  context, and `RaptorLifecycle` resolves to `context.lifecycle`. So injecting a plain
  `CoroutineContext` anywhere yields the lifecycle's context.

`RaptorLifecycle` (`RaptorLifecycle.State`) guards only two transitions via `check()` +
`compareAndSet`: `startIn()` requires state `stopped` (→ `starting`) and `stop()` requires
state `started` (→ `stopping`, then back to `stopped`). These guards are **not** a strict
single-run machine — because `stop()` returns state to `stopped`, they permit
`stopped → starting` *again* after a stop; re-use is only discouraged by a class TODO
("Prevent re-use", because `_coroutineContext` cannot be reused), not enforced. `startIn(scope)`
flips state to `starting` before its first suspension, but then *suspends until every start
action completes* before flipping to `started` (it does not fire-and-forget — see
`modules/lifecycle/tests-jvm/LifecycleTests.kt` `testLifecycleWaitsForActions`), so callers
`launch` it in a scope to observe the intermediate `starting` state. `DefaultLifecycle` builds
its `_coroutineContext` fresh in `startIn()` and nulls it in `stop()`.
