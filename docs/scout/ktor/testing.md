# ktor-test: how the test engine is swapped in

The `ktor-test` module rewires servers to Ktor's in-memory `TestEngine` using non-public
APIs — relevant when writing or debugging Ktor tests. For how to actually drive HTTP
requests against such a server, see `http-integration-tests.md`.

- **`RaptorKtorTestPlugin` swaps every server's engine.** It installs `RaptorKtorPlugin`,
  then inside `ktor.servers.all { }` sets `engine()` to `TestEngine`,
  `applicationEnvironmentFactory(::createTestEnvironment)`, forces `watchPaths = emptyList()`
  (disabling auto-reload file watching), and `startStopDispatcher(Dispatchers.Unconfined)`.
  The whole body is under `@Suppress("INVISIBLE_MEMBER")` because `engine()`,
  `applicationEnvironmentFactory()`, and `startStopDispatcher()` are non-public raptor
  server-configuration APIs. Anchor:
  `modules/ktor-test/sources-jvm/assembly/RaptorKtorTestPlugin.kt`.
- **`RaptorKtorServer.testEngine` returns null unless the test plugin ran.** It is
  `embeddedServer?.engine as? TestApplicationEngine` — a safe cast yielding null for any
  server whose engine is not `TestEngine`, including a fully-running production-engine server
  (not only an unstarted one). Anchor:
  `modules/ktor-test/sources-jvm/api/RaptorKtorServer.test.kt` (`testEngine`).
- **Build note:** the dependency on `ktor("server-test-host")` explicitly excludes
  `org.jetbrains.kotlin:kotlin-test-junit`. Easy to drop during a dependency bump and
  break test wiring.
