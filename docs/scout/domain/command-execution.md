# Aggregate command execution: retry, batching, commit window

Behavioral contracts of `RaptorScope.execution {}` and `commit()`, hidden inside inline
helpers.

- **Version presence silently toggles conflict handling.**
  `RaptorAggregateCommandExecutor.execute(id, version, command)` computes
  `retryOnVersionConflict = (version == null)`. Calling execute *without* a version retries
  on `RaptorAggregateVersionConflict`; passing an explicit version disables retry and
  rethrows the conflict. Anchor: `modules/domain/sources/api/RaptorAggregateCommandExecutor.kt`
  (`execute`).
- **Retry is capped at 100, and the whole action re-runs each time.** The inline
  `execution(retryOnVersionConflict, action)` wraps `run(action) + commit()` in
  `repeat(100)`; after 100 consecutive conflicts it `error()`s ("Did you forget to make sure
  that the expected aggregate version is (re)loaded within the batch action?"). The action is
  invoked AT_LEAST_ONCE and re-executed in full per retry — **side effects inside it run
  repeatedly.**
- **Commands batch per aggregate; one expected version per aggregate per block.**
  `execution {}` accumulates commands into one `AggregateBatch` per aggregate ID
  (insertion-ordered `linkedMapOf`). Passing different non-null versions for the same
  aggregate in one block throws ("version cannot be different for the same aggregate in the
  same batch"); version must be `>= 0`. Aggregates are mutated on a defensive `copy()` of
  stored state before the new state replaces the old. Anchor:
  `modules/domain/sources/implementation/DefaultAggregateManager.kt` (`Execution`, `Commit`).
- **Commit persists before dispatch — a known crash window.**
  `DefaultAggregateManager.commit()` calls `store.add(...)` first, then advances
  `nextEventId`, updates in-memory state, then dispatches. A FIXME documents that if
  `store.add` succeeds but the connection is then lost, events are persisted but never
  dispatched and `nextEventId` is wrong — unrecoverable. `onCommittedActions` run *after* the
  mutex is released, outside the commit lock.
- **`RaptorAggregateVersionConflict` implements kotlinx.coroutines `CopyableThrowable`** and
  overrides `createCopy()` (rebuilding itself with the original set as `cause`). This lets
  kotlinx.coroutines reconstruct a fresh instance that preserves *this* exception's own state
  and a recovered stacktrace when it is re-thrown across a coroutine boundary. It is opt-in per
  exception type — not a general requirement for every exception that crosses coroutine
  machinery.
