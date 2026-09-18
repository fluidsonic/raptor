# Module layout, naming, and build wiring

How Raptor's Gradle multi-module build is structured — needed before adding a module or referencing one.

- **Modules are auto-discovered.** `settings.gradle.kts` scans every subdirectory of
  `modules/` that contains a `build.gradle.kts`, calls `include(dirName)`, then rewrites
  the project: `project(":$name").name = "raptor-$name"` and `projectDir` points at the
  folder. So folder `transactions` becomes Gradle project `:raptor-transactions`.
  Dependency notations must use the `raptor-`-prefixed name; adding a module needs no
  `include` edit — just create the folder and its build file.
- **Non-standard source sets.** Production code lives in `sources-jvm/`, tests in
  `tests-jvm/` (a few multiplatform-shaped modules use `sources/`/`tests/`), never
  `src/main/kotlin` or `src/test/kotlin`. Internal implementation classes sit in
  `sources-jvm/` alongside a public `api/` subpackage.
- **Root `build.gradle.kts` (the `fluidLibrary` block)** globally opts every module into
  the experimental marker `io.fluidsonic.raptor.RaptorInternalApi` and calls `disableDokka()`
  (renamed from `noDokka()` in plugin 4.x). This is why modules use `@RaptorInternalApi`
  members without a local `@OptIn`.
- **Context-sensitive resolution is enabled build-wide.** The `subprojects {}` block adds
  `-Xcontext-sensitive-resolution` to every Kotlin multiplatform subproject (every
  `fluidLibraryModule` is multiplatform, even JVM-only ones). It previously also added
  `-Xcontext-parameters`, dropped after the plugin bump because Kotlin 2.4 reports that flag
  redundant — context parameters no longer need an opt-in flag. Code using `context(...)` —
  e.g. `context(coroutineScope: CoroutineScope)` in
  `modules/event/sources/api/RaptorEventSource.kt` and on `DefaultAggregateManager.start`
  (`modules/domain/sources/implementation/DefaultAggregateManager.kt`) — still needs
  `-Xcontext-sensitive-resolution` set at the root, not per module.
- Built with the third-party `io.fluidsonic.gradle` plugin, applied in the root
  `build.gradle.kts` (version 4.2.0 as of this pass). The project's own declared version
  lives in that file's `fluidLibrary(...)` call.

Deprecated modules slated for deletion: `entities`, `entities-core` (their build files carry
`// TODO Deprecated module. Delete.`).
