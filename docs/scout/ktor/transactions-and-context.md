# Ktor transactions, per-route nesting, and the `raptorContext` split

How Raptor transactions attach to Ktor requests and how context is exposed — plus a
framework-bug workaround you must not "optimize" away.

- **`raptorContext` resolves to two different types by receiver.**
  `Application.raptorContext` returns a server-scoped `RaptorContext` (via
  `raptorServerInternal.context`), while `ApplicationCall`/`PipelineContext`/`RoutingContext`/
  `WebSocketServerSession.raptorContext` return a per-request **`RaptorTransactionContext`**
  (via `raptorTransaction.context`). `raptorServerInternal` reads the server off
  `attributes.getOrNull(Keys.serverKtorAttribute)` and throws `RaptorPluginNotInstalledException`
  if absent. Anchor: `modules/ktor/sources-jvm/api/Ktor.kt` (all `raptorContext` extensions).
- **Transaction lifecycle is split across two plugins plus per-route nesting.**
  `RaptorTransactionKtorPlugin` creates the top-level transaction on `CallSetup` (stored under
  a private `AttributeKey`, with the `ApplicationCall` registered into the transaction's
  property registry under `ktorCallPropertyKey`) and removes it on `CallFailed` (rethrowing)
  and `ResponseSent`. Separately, each route's `transactionRoutePlugin` intercepts
  `ApplicationCallPipeline.Setup` to create a **child** transaction
  (`properties.withFallback(parentContext.properties)`), swap `call.raptorTransaction` to it, run `proceed()` in
  a `coroutineScope`, and restore the parent in `finally`. A request accumulates nested
  transactions, one per configured route level.
- **`di.get<ApplicationCall>()` works only inside a Raptor transaction.**
  `RaptorKtorServerComponent.onConfigurationStarted` registers a DI provider that resolves
  `get<RaptorTransactionContext>().ktorCall` (errors "Cannot find Ktor ApplicationCall." if
  absent) — the call is placed there by `RaptorTransactionKtorPlugin`.

**Do not hoist the per-route transaction plugin to a single shared instance.**
`transactionRoutePlugin()` gives each installed plugin a unique name embedding an
incrementing counter (`"RaptorRouteTransactionPlugin.$uniqueId"`, `nextRoutePluginId++`). A
comment (tested in Ktor 3.3.1) explains that reusing one plugin name causes it to run only on
child routes and **not** on a parent route where it is also installed; unique names force the
parent to execute. Anchor: `implementation/RaptorKtorServerInternal.kt`
(`transactionRoutePlugin`, `nextRoutePluginId`).
