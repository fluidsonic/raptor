# Writing HTTP integration tests against a Raptor Ktor server

How to drive real HTTP requests through the in-memory test engine. The wiring is
non-obvious and spread across modules; first used in
`modules/ktor-graph/tests-jvm/RouteHttpTests.kt` (the `withEngine` helper).

- Build the server with `raptor { install(RaptorKtorTestPlugin); … ktor.servers.new { … } }`,
  then boot it inside `runTest` with `raptor.lifecycle.startIn(this)` (requires
  `RaptorLifecyclePlugin`). Because `RaptorKtorTestPlugin` forces
  `startStopDispatcher(Dispatchers.Unconfined)`, startup completes inline before the next
  line runs. Reach the engine via `raptor.context.ktor.servers.single().testEngine` (see
  `testing.md` for how that engine is swapped in).
- **Send requests through `engine.client` (a Ktor `HttpClient`)** — `client.get(...)`,
  `client.post { contentType(...); setBody(...) }`, and read the body with
  `response.bodyAsText()`. `TestApplicationEngine.handleRequest` is `internal` in ktor and
  cannot be called from another module, so the client is the only cross-module entry point.
- **Set `unencryptedHosts(setOf("localhost"))` inside `ktor.servers.new { }`** or every
  plain-HTTP test request comes back 400 "The connection protocol is not secure." from
  `EncryptionEnforcementKtorPlugin`
  (`modules/ktor/sources-jvm/ktor/EncryptionEnforcementKtorPlugin.kt`), which rejects any
  non-`https`/`wss` scheme whose host is not whitelisted.

version: ktor 3.4.2 (as of 2026-07-22)
