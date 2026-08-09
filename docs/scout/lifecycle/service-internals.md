# RaptorService shutdown choreography and registration DSL

Internal mechanics of a `RaptorService` instance and how service constructors get injected.

- **A keep-alive coroutine holds the supervisorScope open.** `RaptorService.Instance` is built
  inside `scope.launch { supervisorScope { ... } }` (marked `// FIXME hack`); its
  `coroutineContext` *is* that supervisorScope's, so any job the service launches blocks the
  scope from completing. Init launches a dedicated coroutine that just awaits
  `shutdownCompletedSignal` — this deliberately keeps an otherwise-idle service alive. `stop()`
  is a four-step handshake: `shutdownStartedSignal.complete()` (fires `cancelOnStop()`) →
  `stopped()` runs → `shutdownCompletedSignal.complete()` (releases keep-alive) → await
  `completionSignal` (completed by the outer `finally`). Anchor:
  `modules/lifecycle/sources-jvm/api/RaptorService.kt` (`createIn`, `Instance`).
- **Shutdown-timeout invariant.** Hardcoded `stopCancelTime = 5.minutes`,
  `stopChildJobsWarnTime = 30.seconds`, `stopHandlerWarnTime = 30.seconds`; a comment requires
  `stopCancelTime >= stopChildJobsWarnTime + stopHandlerWarnTime`. Tuning one alone breaks it.
- **`Status.failed` is terminal and short-circuits.** Once failed, `stop()` returns
  immediately (no `stopped()` call). `handleException()` also silently returns (no
  `exceptionRaised()` call) for `failed` or `stopping`; it hard-errors only for
  `created`/`creating`/`new`/`stopped`. `failed` is entered when `created()` throws, the
  `exceptionRaised()` handler throws, or `stop()` fails.
- **`service()` constructor auto-wiring is 21 hand-written overloads** (`service0`..`service20`
  via `@JvmName`) taking `KFunction0..KFunction20`, each expanding to `service {
  factory(get(), get(), ...) }` with one DI `get()` per parameter, resolved positionally by
  type — capping constructor injection at **20 parameters.**
- **`provides(KClass)`** has an inline `<reified Type : KClass<in Service>>` overload that
  extracts the type argument via `typeOf<Type>().arguments.single().type` to simulate a lower
  bound (comment cites JetBrains **KT-209**), letting a service register under a supertype key.

Service DI keys compare by **object identity, not by name.** `ServiceDIKey`
(`modules/lifecycle/sources-jvm/implementation/ServiceDIKey.kt`) deliberately does *not* override
`equals`/`hashCode` — unlike `KTypeDIKey`, which compares by `KType`. The single `diKey` built in
`RaptorServiceRegistration.install` is threaded into both the DI provider and the
`RaptorServiceController` lookup so they match; a freshly constructed `ServiceDIKey(sameName)`
will *not* resolve an already-registered service.
