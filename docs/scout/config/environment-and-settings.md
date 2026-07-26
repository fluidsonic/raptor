# Environment and settings modules (dotenv-backed, assembly-time)

How runtime configuration actually flows in. Both modules wrap dotenv-kotlin
(`io.github.cdimascio:dotenv-kotlin`, version: 6.5.1 as of 2026-07-22 — see `Versions.dotenv`
in `buildSrc/sources/Versions.kt`) and both are consumed at assembly time, not injected via DI.

**`environment` module** — `RaptorEnvironment` is a **process-wide singleton**, exposed only
on `RaptorGlobalDsl` and always returning `RaptorEnvironment.instance`. It wraps
`io.github.cdimascio` dotenv-kotlin with `ignoreIfMissing = true`, so a missing `.env` file is
tolerated; a missing *required* variable throws lazily at `get()`. `scope(key, separator)`
returns a new `RaptorEnvironment` sharing the SAME underlying `Dotenv` object and only
prepending a prefix to every lookup. Anchor:
`modules/environment/sources-jvm/RaptorEnvironment.kt` (`scope`).

**`settings` module** — backed by the same dotenv-kotlin dependency (`Versions.dotenv`):
- **`env()` snapshots values eagerly, once per process.**
  `EnvRaptorSettingValueProvider.value` is a plain property initializer (`= dotenv[name] ?:
  default`), so the env var is captured at ValueProvider construction; the file-level `private
  val dotenv = dotenv { ignoreIfMissing = true }` is evaluated once at class load. Env values
  are frozen at assembly time; later environment/.env changes are never picked up (file TODO
  "Rework this"). Anchor: `modules/settings/sources-jvm/EnvRaptorSettingValueProvider.kt`.
- **`convert` coerces only String→Int.** The top-level private `convert` function in
  `modules/settings/sources-jvm/RaptorSettings.kt` parses a String requested as Int via
  `toIntOrNull()`; every other type request goes through `KClass.safeCast` and `error()`s on
  mismatch. No general coercion.
- **`Builder.set`** stores a `ValueProvider<*>` as-is, else wraps the value via
  `ValueProvider.constant(...)`. Nested `"parent" { "child" by ... }` blocks work because the
  child `RaptorSettings` is wrapped as a constant provider. `MapRaptorSettings.valueProvider`
  resolves a dotted path by splitting on the first `.` and recursing into the child
  `RaptorSettings`; a non-settings intermediate silently returns null.
- **`install(settings)` can be called only once** ("Cannot set settings multiple times"). Layer
  sources by pre-combining with `RaptorSettings.lookup(...)` (`LookupRaptorSettings`, first
  non-null provider wins).
- **`RaptorSettingsPlugin.install` is an empty no-op placeholder** (`// TODO Actually use`);
  settings are read off the component-registry root during assembly, not via runtime DI.
