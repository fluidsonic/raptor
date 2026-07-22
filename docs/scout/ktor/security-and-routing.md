# Ktor: encryption-by-default, route wrapping, error model

- **Servers enforce encryption by default.** `RaptorKtorServersComponent.new(
  forceEncryptedConnection = true)` defaults to installing `EncryptionEnforcementKtorPlugin`,
  which rejects any call whose `origin.scheme` is not `https`/`wss` with a 400 ("The
  connection protocol is not secure.") unless `origin.serverHost` is listed via
  `unencryptedHosts(...)`. There is no global disable other than passing
  `forceEncryptedConnection = false` per server. Local http testing needs one of those two
  opt-outs. Anchors: `modules/ktor/sources-jvm/assembly/RaptorKtorServersComponent.kt` (`new`),
  `modules/ktor/sources-jvm/assembly/RaptorKtorServerComponent.kt` (`unencryptedHosts`).
- **The https check silently depends on `XForwardedHeaders` being installed first.**
  `EncryptionEnforcementKtorPlugin` reads `call.request.origin`, which only reflects the real
  client scheme/host behind a proxy because `Application.configure()` installs
  `XForwardedHeaders` earlier. Required order: `XForwardedHeaders` → `EncryptionEnforcementKtorPlugin`
  (only when forcing encryption) → `RaptorTransactionKtorPlugin`. Reorder/drop and every proxied
  request is judged on the proxy's local (plain http) scheme. Anchors: the install order in
  `modules/ktor/sources-jvm/implementation/RaptorKtorServerInternal.kt` (`Application.configure()`),
  the origin read in `modules/ktor/sources-jvm/ktor/EncryptionEnforcementKtorPlugin.kt`.
- **Route wrappers nest, earliest = outermost.** `RaptorKtorRouteComponent.wrap` composes
  multiple `wrap {}` blocks as `{ next -> previousWrapper { wrapper(next) } }`, so an
  earlier-registered wrapper is outermost and later ones nest inside it around the handler.
- **`ServerFailure` error model.** Carries four strings (`code`, `developerMessage`,
  `internalMessage`, `userMessage`). `internal(cause)` sets `code = "internal"` and both
  `userMessage` and `developerMessage` to the same generic string; only `internalMessage`
  keeps `cause.message`. So for internal failures `developerMessage` is *not* diagnostic —
  only `internalMessage` is. (This model drives the GraphQL error mapping — see
  `graphql/exception-handling.md`.) The class is flagged untested (`// TODO Add tests`).

Root path quirk: user root routes register path `"/"`, but the synthetic server-level
container route uses empty path `""` with `host = null` — two different "root" strings.

Route DSL: `RaptorAssemblyQuery<...Root>.new(host)` extensions need `@JvmName("rootNew")` to
avoid a JVM signature clash (erasure) with the `new(path, host)` overloads.
</content>
