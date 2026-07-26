# DI builder validation, reflective injection, and the three scopes

Registration-time rules and the module's three distinct DI scopes.

- **`validateType` forbids several types at registration.**
  `DefaultRaptorDIBuilder.validateType` rejects providing: nullable types, `Any`, `Unit`,
  any subclass of `RaptorContext`, and any type whose arguments contain a `KTypeParameter`
  (so you cannot `provide<Box<T>>` from inside a generic function — the type must be
  concrete). Validation only runs for `KTypeDIKey`; `LabeledDIKey` bypasses it. These are
  runtime `error()`/`require()` failures, not compile-time. Anchor:
  `modules/di/sources-jvm/di/DefaultRaptorDIBuilder.kt` (`validateType`).
- **`provide(KFunction)` does reflective constructor injection.** It reads
  `factory.parameters`, turns each into a `RaptorDIKey`, then `get()`s each and calls
  `factory.call(*array)`. This reflective call — together with `isSubclassOf` in the
  builder and the `KType`-based keys — is why `modules/di/build.gradle.kts` adds
  `implementation(kotlin("reflect"))`. Anchor:
  `modules/di/sources-jvm/assembly/RaptorDIComponent.kt` (the `provide(factory: KFunction<Value>)` overload).
- **Three parallel DI component implementations**, each keyed differently and wired into a
  different lifecycle point (`modules/di/sources-jvm/assembly/`):
  - `RootDIRaptorComponent` (key `"DI"`, installed by `RaptorDIPlugin`) registers the
    app-level DI into the property registry via `createDI<RaptorContext>` in
    `onConfigurationEnded`.
  - `RaptorDIFactoryComponent` (key `"DI factory"`, lazily `oneOrRegister`ed) exposes
    `toFactory(name)` rather than registering a DI.
  - `TransactionDIRaptorComponent` (key `"transaction DI"`) builds a factory in
    `onConfigurationEnded`, stores it under a per-component `factoryPropertyKey`
    (`"transaction DI factory"`), and defers actual DI creation to an `onCreate` hook that
    reads that factory out of `parentContext[factoryPropertyKey]` (errors "Cannot find
    dependency injection factory." if absent).
- **`di` is one identifier overloaded across five receivers** (`RaptorDIBoundary`,
  `RaptorPluginScope<in RaptorDIPlugin>`, `RaptorTransactionsComponent`, a mapped
  `RaptorAssemblyQuery<RaptorTransactionsComponent>`, and `RaptorScope`) — which one you
  get depends purely on the static receiver type. The `RaptorScope.di` overload
  (`modules/di/sources-jvm/di/RaptorDI.kt`) is the odd one out: it returns the runtime
  `RaptorDI` container, not a configuration component, and throws
  `RaptorPluginNotInstalledException` if unconfigured.
- **`RaptorDIBoundary` is `@RaptorInternalApi`**; its `diFactory(name)` silently returns
  `RaptorDI.Factory.empty` when unconfigured (TODO to throw) — inconsistent with the
  plugin accessors elsewhere that throw `RaptorPluginNotInstalledException`.
