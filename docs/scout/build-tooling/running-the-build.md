# Building raptor: invoke the wrapper as the literal `./gradlew`

How an agent must invoke the build here, and why every other Java invocation fails in a way that
looks like a broken machine rather than a sandbox restriction.

- **Invoke the wrapper as the literal relative path `./gradlew` from the repository root.**
  `.claude/settings.json` lists `./gradlew:*` under `sandbox.excludedCommands`, and entries are
  matched against the command text, so the very same wrapper invoked through an absolute path is
  not excluded, runs sandboxed and dies before Gradle starts with "Unable to locate a Java
  Runtime".
- **Nothing else Java-based runs from a sandboxed command.** `java`, `javap` and even
  `/usr/libexec/java_home` fail with that same message inside the sandbox although the machine has
  a Java runtime — which is what `./gradlew` needs its exclusion for. Consequence: anything needing
  a Java runtime (running tests, inspecting a dependency's class files) has to go through a
  `./gradlew` task; use `unzip` if you only need to look inside a jar from the Gradle cache.
- **Test tasks are `:raptor-<folder>:jvmTest`** — for example
  `./gradlew :raptor-graph:jvmTest --tests 'tests.InvalidInputTests'`. The `raptor-` prefix comes
  from `settings.gradle.kts` (see `project-layout.md`) and the task is `jvmTest` rather than `test`
  because every module is a Kotlin multiplatform project. Pass `-i` to see what a test prints;
  several graph tests record their client-facing payload that way and assert nothing about it.

See `project-layout.md` for module naming and the Gradle plugin itself.
