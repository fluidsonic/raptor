# Gradle does not forward `-D` properties into forked test JVMs

Why a `System.getProperty(...)`-gated opt-in test flag needs an explicit forwarding
block in the module's `build.gradle.kts`, or the flag is permanently un-openable.

Gradle's `Test` task runs in a forked JVM by default, and that fork does **not**
inherit `-D` properties passed to the Gradle build's own JVM (`./gradlew
-Draptor.bson.benchmarks=true ...`). Without an explicit forward, `System.getProperty(...)`
inside the test JVM sees nothing — no error, the test just silently runs its "skipped"
branch, which reads exactly like the flag doing nothing on a machine that has no
runtime issue at all. This is why `modules/bson/build.gradle.kts` adds a
`tasks.withType<Test>().configureEach { ... }` block that reads `raptor.bson.benchmarks`
from the build JVM via `System.getProperty` and re-declares it on the task via
`systemProperty(...)` — only that named property crosses the fork, nothing else does
automatically.

`modules/bson/tests-jvm/BsonBenchmarks.kt` is the consumer: its `benchmarksEnabled` val
reads the same property name inside the test JVM, and both `@Test` methods
(`reportDecodeBenchmarks`, `reportEncodeBenchmarks`) early-return with a "Skipping..."
println when it isn't `"true"`.

**Trap for future opt-in flags:** adding a new `System.getProperty`-gated test elsewhere
in the repo needs the identical forwarding block (with that property's name) in the
owning module's `build.gradle.kts`, or the documented `-D...` command silently does
nothing when run. `raptor-bson` is currently the only module with this forwarding block.

Related: running-the-build.md (task naming), bson/bson-types.md (what
`BsonBenchmarks.kt` measures).
