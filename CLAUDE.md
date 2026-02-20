# Raptor Framework — AI Instructions

## Skills

Always load the `superpowers:using-superpowers` skill at the start of every conversation.

## Project Structure
- Modules in `modules/<name>/`, Gradle module names: `:raptor-<name>`
- Explicit API mode — all declarations need visibility modifiers
- Versions centralized in `buildSrc/`
- Context parameters enabled: `-Xcontext-parameters` and `-Xcontext-sensitive-resolution` in root `build.gradle.kts`

## Documentation
- `README.md` — project overview, module list, architecture rules
- `modules/service2/README.md` — service2 architecture, design decisions, quality requirements
- Always consult relevant module READMEs before making changes to that module

## Code Style
- Two blank lines after last import
- Two blank lines between functions (except bodyless)
- Sort functions alphabetically unless a more specific convention applies
- Use `assertEquals` with named parameters, `actual` before `expected`
- No obscure abbreviations — never `ctx`, `msg`, `req`, `res`, etc.; always use full names
- Enum values must be lowercase (they are values like `val`, not types)
- Omit curly braces for trivial single-statement `if`/`else` bodies
- `return` statements must be on their own line
- `?: return` must also be on its own line (as a continuation)
- Never use `!!` — use `checkNotNull()` or `?: error()` instead
- Blank line before `try`
- Blank line before `if`/`else` if there are more statements after it
- Expression-body functions: implementation after `=` must be on its own line

## Architecture Rules
- **NEVER introduce new `ThreadLocal` usage. This is a hard project rule.** Coroutines resume on different threads, making `ThreadLocal` silently lose state. Use `CoroutineContext.Element` for coroutine-scoped state, or constructor injection via DI.
- Known legacy `ThreadLocal` bugs to be replaced:
  - `modules/graph/sources-jvm/graphql/resolution/ArgumentResolver.kt:12`
  - `modules/service2/sources-jvm/api/RaptorServiceWorker.kt:79` (`CurrentServiceWorker`)
- Prefer constructor injection via DI over any form of ambient context
- Services must not get direct `CoroutineScope` access — use `RaptorServiceWorker` instead
- Two-phase event processing: middleware (synchronous, indexes) runs before service handlers (asynchronous)

## Quality Rules
- No `@Suppress` without written justification
- No deprecated API usage in new code
- Favor compile-time safety over runtime checks
- Do not change non-test code without prior approval

## Module Status
- **Legacy modules:** `graphql`, `entities`, `entities-core` — older APIs, avoid for new work
- **Empty/stub modules:** `mongo2`, `key-value-store-mongo2` — no source files

## Raptor Plugin Pattern
- Plugins: `object XPlugin : RaptorPlugin` with `fun RaptorPluginInstallationScope.install()`
- DI access: `RaptorScope.di` (requires `import io.fluidsonic.raptor.di.*`)
- Service registration: `service2(::MyService) { ... }` DSL in plugin install

## Service2 Module
- Declarative input-source model: `onEvent()`, `onAggregateChanges()`, `onStart()`, etc.
- Operators: `map`, `filter`, `flatMap`, `batchBy`, `delay`, `delayUntil`, `waitFor`
- Middleware: synchronous event interceptors for indexes, run before async service handlers
- Error strategies: `onError().log()`, `onError().stopService()`, `onError().stopLifecycle()`
- See `modules/service2/README.md` for architecture details, design decisions, and quality requirements

## Testing
- Use `kotlinx-coroutines-test` with `runTest` for coroutine tests
- Use `CoroutineStart.UNDISPATCHED` when subscribing to `SharedFlow` in tests
- Test through public API, not by duplicating internal logic

## Gradle
- Build: `./gradlew :raptor-<module>:compileKotlin`
- Test: `./gradlew :raptor-<module>:test`
