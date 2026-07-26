# RaptorService shutdown choreography and registration DSL

Internal mechanics of a `RaptorService` instance and how service constructors get injected.

- **A keep-alive coroutine holds the supervisorScope open.** `RaptorService.Instance` is built
  inside `scope.launch { supervisorScope { ... } }` (marked `// FIXME hack`); the Instance's
  `coroutineContext` *is* that supervisorScope's context, so any job the service launches is a
  child that blocks the scope from completing. In init the Instance launches a dedicated
  keep-alive coroutine that just awaits `shutdownCompletedSignal` — this is what deliberately
  keeps an otherwise-idle service alive. `stop()` drives a four-step handshake over three
  signals:
  `shutdownStartedSignal.complete()` (fires `cancelOnStop()` cancellations) → `stopped()` runs →
  `shutdownCompletedSignal.complete()` (releases keep-alive) → await `completionSignal`
  (completed by the outer `finally` once scope + children finish). Anchor:
  `modules/lifecycle/sources-jvm/api/RaptorService.kt` (`createIn`, `Instance`).
- **Shutdown-timeout invariant.** Hardcoded `stopCancelTime = 5.minutes`,
  `stopChildJobsWarnTime = 30.seconds`, `stopHandlerWarnTime = 30.seconds`, with a comment that
  `stopCancelTime` must be `>= stopChildJobsWarnTime + stopHandlerWarnTime`. Tuning one in
  isolation silently breaks it.
- **`Status.failed` is terminal and short-circuits.** Once failed, `stop()` returns
  immediately (no `stopped()` call) and `handleException()` returns without invoking
  `exceptionRaised()`. `failed` is entered when `created()` throws, the `exceptionRaised()`
  handler throws, or `stop()` fails. `handleException()` hard-errors if a child exception
  arrives while status is anything other than `started`/`failed`.
- **`service()` constructor auto-wiring is 21 hand-written overloads** (`service0`..`service20`
  via `@JvmName`) taking `KFunction0..KFunction20`, each expanding to `service {
  factory(get(), get(), ...) }` with one DI `get()` per parameter, resolved positionally by
  type — capping constructor injection at **20 parameters.**
- **`provides(KClass)`** has an inline `<reified Type : KClass<in Service>>` overload that
  extracts the type argument via `typeOf<Type>().arguments.single().type` to simulate a lower
  bound (comment cites JetBrains **KT-209**), letting a service register under a supertype key.

Service DI keys compare by **object identity, not by name.** `ServiceDIKey`
(`modules/lifecycle/sources-jvm/implementation/ServiceDIKey.kt`) deliberately does *not* override
`equals`/`hashCode` — unlike `KTypeDIKey`, which compares by `KType`. So the single `diKey`
instance built in `RaptorServiceRegistration` (the `install` function) is threaded into both the
DI provider and the `RaptorServiceController` lookup so they match; a freshly constructed
`ServiceDIKey(sameName)` will *not* resolve an already-registered service.
