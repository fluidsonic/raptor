# Bare `compileKotlin` is ambiguous — always target the platform explicitly

Matters when scripting a compile-only check without running the full test task.

Every one of the 26 module directories under `modules/` is a Kotlin Multiplatform project (see
`project-layout.md`) and declares at least a `jvm` target (`jvm()` or `jvm { ... }` in its
`build.gradle.kts`); 7 also declare `common`, and 2 (`dsl`, `entities-core`) also declare `js`.
Because of this, `./gradlew compileKotlin` fails as ambiguous — Gradle lists
`compileKotlinJvm`/`compileKotlinJs`/`compileKotlinMetadata` as candidates instead of picking one.
Use the explicit per-target task names: `compileKotlinJvm compileTestKotlinJvm` compiles
production and test sources for the JVM target without running tests (`jvmTest`, documented in
`running-the-build.md`, both compiles and runs).

Related: `running-the-build.md`, `project-layout.md`.
