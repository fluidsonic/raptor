# A Kotlin version bump can break `check` on the JS Yarn lockfile

Why `./gradlew check` can fail right after bumping the `io.fluidsonic.gradle` plugin (and the
Kotlin version it brings in), even though nothing JS-related was touched.

- `check` runs `:kotlinStoreYarnLock`, one of the Kotlin Multiplatform Gradle plugin's own
  Yarn/JS tasks. It fails with "Lock file was changed. Run the `kotlinUpgradeYarnLock` task to
  actualize lock file" whenever the new Kotlin version pulls in different versions of its
  bundled JS tooling dependencies (webpack, karma, etc.), which changes what the generated
  lockfile should contain.
- Fix: run `./gradlew kotlinUpgradeYarnLock`, then commit the regenerated
  `kotlin-js-store/yarn.lock` — it is tracked in this repo, not generated fresh per machine, so
  a stale committed copy is what trips the check after any Kotlin bump.
- This is unrelated to the module's own JVM/multiplatform test setup; it fires even for
  JVM-only modules, because Kotlin Multiplatform's JS/Yarn tooling is wired in at the Gradle
  root regardless of which targets a given module declares.

See `project-layout.md` for the plugin version and `running-the-build.md` for how to invoke
`./gradlew` in this environment.
