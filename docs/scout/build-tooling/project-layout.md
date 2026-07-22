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
  the experimental marker `io.fluidsonic.raptor.RaptorInternalApi` and calls `noDokka()`.
  This is why modules use `@RaptorInternalApi` members without a local `@OptIn`.
- **Context parameters are enabled build-wide.** The `subprojects {}` block adds
  `-Xcontext-parameters` and `-Xcontext-sensitive-resolution` to every Kotlin
  multiplatform subproject (every `fluidLibraryModule` is a multiplatform project, even
  JVM-only ones). Code using the `context(...)` declaration — e.g. `context(di: RaptorDI)`
  in `modules/di/sources-jvm/di/RaptorDI.kt` and the `context(RaptorDI) () -> ...` factory
  types in `modules/domain/sources/assembly/RaptorAggregatesComponent.kt` — depends on
  these experimental flags being set at the root, not per module. (Note: the BSON codec's
  `with(scope){...}` receiver style in `modules/bson/sources-jvm/bson/RaptorBsonCodec.kt`
  is ordinary Kotlin scoping and does not need these flags.)
- Built with the third-party `io.fluidsonic.gradle` plugin, applied in the root
  `build.gradle.kts` (version 3.0.0 as of this pass). The project's own declared version
  lives in that file's `fluidLibrary(...)` call.

Deprecated modules slated for deletion: `graphql`, `entities`, `entities-core` (their
build files carry `// TODO Deprecated module. Delete.`).
</content>
