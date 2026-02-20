# Raptor

Kotlin framework for event sourcing, DDD, GraphQL, and Ktor integration.

## Architecture

### Module Organization
Modules live in `modules/<name>/`. Gradle module names follow `raptor-<name>` convention.

Key modules:
- **core** — base types, plugin system, component registry
- **di** — dependency injection
- **domain** — aggregates, projections, event sourcing
- **lifecycle** — service lifecycle management (v1)
- **service2** — declarative service framework (v2, replacing lifecycle services)
- **ktor** — Ktor server integration
- **graph/graphql** — GraphQL schema and resolution

### Plugin System
All features are composed via `RaptorPlugin`. Plugins declare dependencies, register components, and hook into lifecycle phases.

### Service Architecture
- **v1** (`lifecycle` module): Abstract class inheritance, manual lifecycle hooks
- **v2** (`service2` module): Declarative input sources, operator composition, automatic lifecycle — preferred for new services

### Key Design Rules
- **No `ThreadLocal`** — coroutines can resume on different threads, making `ThreadLocal` silently lose state. Use `CoroutineContext.Element` for coroutine-scoped state or constructor injection via DI. Known legacy bugs: `graph/ArgumentResolver` (line 12), `service2/RaptorServiceWorker` (`CurrentServiceWorker`, line 79).
- **No raw `CoroutineScope` in services** — services receive `RaptorServiceWorker`, not `CoroutineScope`. This prevents unstructured concurrency and ensures lifecycle management.
- **Explicit API mode** — all public declarations require visibility modifiers.
- **Constructor injection** — prefer DI constructor injection over ambient context.
- **Context parameters** — `-Xcontext-parameters` and `-Xcontext-sensitive-resolution` are enabled project-wide.
- **Two-phase event processing** — middleware runs synchronously first (indexes), then service handlers run asynchronously. Indexes are always current when services query them.

## License
Apache 2.0
