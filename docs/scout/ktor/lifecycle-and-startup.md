# Ktor server lifecycle: extreme priorities, bespoke startup, single-shot

Startup/shutdown wiring for the Ktor server that isn't visible from the public API.

- **Server start/stop hooks use opposite extreme priorities.** `RaptorKtorPlugin.install`
  registers `onStart("ktor server", priority = Int.MIN_VALUE)` and `onStop("ktor server",
  priority = Int.MAX_VALUE)`, so the HTTP server boots after all other start hooks and shuts
  down before all other stop hooks (never accepting traffic before dependencies are ready or
  after they are torn down). Ordering relies on the lifecycle-priority convention (see
  `lifecycle/service-orchestration.md`). Anchor:
  `modules/ktor/sources-jvm/assembly/RaptorKtorPlugin.kt`.
- **Configuration runs through a monitor subscription, not a Ktor module block.**
  `startServerBlocking()` subscribes to the `ApplicationStarting` monitor event only *after*
  `embeddedServer()` is constructed (comment: the engine's own subscriptions must be
  processed first), runs `Application.configure()` inside that handler capturing any
  `Throwable`, then `server.start(wait = false)` and disposes the subscription. If
  `configure()` threw, it stops the server (grace/timeout 0), attaches any stop failure as
  a suppressed exception on the original, and rethrows the original. Anchor:
  `modules/ktor/sources-jvm/implementation/RaptorKtorServerInternal.kt` (`startServerBlocking`).
- **Start/stop are single-shot atomicfu state machines.** `RaptorKtorInternal` and
  `RaptorKtorServerInternal` hold an `atomic<State>`; `start()` requires `initial`, `stop()`
  requires `started`, else `check()` throws. `stopped` is terminal — no restart. Server stop
  uses a hardcoded 1000 ms grace period.
- **Default engine is Netty with hand-tuned codec.** When no `engine()` is configured, the
  server defaults to Netty with `responseWriteTimeoutSeconds = 30` and a custom
  `HttpServerCodec` whose max initial line length is `4 * 4096` (16384, vs Netty's default
  4096) — allowing longer request lines/URLs. Silent defaults in
  `RaptorKtorServerComponent.onConfigurationEnded`.

**Duplicate-install trap:** `RaptorKtorServerComponent.install()` guards with
`if (plugins.add(plugin)) { ...onConfigurationStarted() }`, but `plugins` is a `MutableList`
whose `add` always returns true. The `if (collection.add)` idiom reads like Set-based
idempotency but is a no-op here — installing the same plugin twice runs its callbacks twice.
</content>
