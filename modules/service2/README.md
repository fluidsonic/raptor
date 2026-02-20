# raptor-service2

Declarative, event-driven service framework replacing the v1 abstract-class-based `RaptorService`.

## Architecture

### Core Concepts
- **`RaptorService2`** — interface for service implementations. Services are self-registering via companion `RaptorPlugin`.
- **`RaptorServiceInput2<Value>`** — declarative event source. All service behavior is defined by subscribing input sources to handlers.
- **Operators** — composable transformations on input sources: `map`, `mapNotNull`, `filter`, `flatMap`, `flatMapFlow`, `batchBy`, `delay`, `delayUntil`, `waitFor`.
- **`RaptorStreamMiddleware`** — synchronous event interceptors for building indexes. Run before async service handlers to ensure indexes are current when services query them.
- **`RaptorServiceWorker`** — async work scheduling interface. Services MUST use this instead of raw `CoroutineScope`.
- **Error strategies** — per-service configurable: `onError().log()`, `onError().stopService()`, `onError().stopLifecycle()`, or custom handler.

### Event Processing Model
Event processing uses a two-phase approach:

1. **Middleware phase (synchronous)** — `RaptorStreamMiddleware` implementations intercept every aggregate projection event. Middleware builds and maintains indexes (e.g., lookup maps backed by `ConcurrentHashMap`). This phase completes before services see the event.
2. **Service phase (asynchronous)** — service handlers run after middleware. Because indexes are already updated, services can query them safely without stale-data races.

This eliminates the need for `blockUntilHandled()` patterns — indexes are always current when services execute.

### Subscription Engine
The subscription engine (`RaptorServiceSubscriptionEngine`) interprets the input source AST at startup:

- Input sources are **data classes** forming a declarative AST (e.g., `FilteredInputSource`, `TransformedInputSource`, `BatchByInputSource`). They describe *what* to subscribe to, not *how*.
- At runtime, the engine **walks the AST** recursively to create coroutine-based subscriptions.
- All `SharedFlow` subscriptions use **`CoroutineStart.UNDISPATCHED`** to ensure the coroutine subscribes before returning control. This prevents missed events between subscribe and first collect, and is critical for `TestCoroutineScheduler` correctness.

### Lifecycle Ordering
1. **`createServices2()`** — DI creates all service instances in parallel (supports 0-20 constructor parameters, auto-wired from DI).
2. **`startMiddleware2()`** — middleware subscribes to the aggregate projection stream.
3. **`startServices2()`** — services subscribe to their declared input sources.
4. **`notifyAggregatesLoaded()`** — unblocks services waiting on historical replay completion.
5. **`stopServices2()`** — cancels all service scopes, joins child jobs.


## Design Decisions

### Hard Rules
1. **No `ThreadLocal`** — `CurrentServiceWorker` at `RaptorServiceWorker.kt:79` is a legacy bug to be replaced. Coroutines resume on different threads, making `ThreadLocal` silently lose state. Use DI constructor injection or `CoroutineContext.Element`.
2. **No raw `CoroutineScope`** — services get `RaptorServiceWorker`, not `CoroutineScope`. This prevents unstructured concurrency and ensures lifecycle-scoped cancellation.
3. **Interface-based** — `RaptorService2` is an interface, not an abstract class. Enables composition over inheritance.

### Architectural Choices
4. **Middleware vs Services** — index-building is middleware (synchronous, before services). Services are async and query indexes safely. Separation is enforced by type: `RaptorStreamMiddleware` vs `RaptorService2`.
5. **Declarative input sources** — sources are data classes forming an AST, not imperative subscriptions. The subscription engine interprets the AST at runtime, decoupling declaration from execution.
6. **Self-registering services** — services are `RaptorPlugin` companions, registered via `service2(::MyService) { ... }` DSL in `install()`. Not registered externally.
7. **Handler references** — type-safe via `ServiceClass::methodName` (KFunction references). The engine resolves handler arity and parameters at registration time.
8. **Constructor injection** — auto-wired from DI (supports 0-20 parameters). No ambient context, no service locator.

### Concurrency Model
9. **`CoroutineStart.UNDISPATCHED`** — used pervasively for `SharedFlow` subscriptions. Ensures the coroutine subscribes synchronously before control returns. Without this, events emitted between `launch` and first `collect` would be lost. Critical for `TestCoroutineScheduler`.
10. **Per-service scope** — each service gets its own `CoroutineScope`. Cancellation is service-scoped; one failing service does not tear down others (unless error strategy says otherwise).
11. **Batch state** — `batchBy` operator uses `ConcurrentHashMap` with version tracking for per-key debouncing. State is scoped to the subscription lifetime.


## Quality Requirements
- All service handlers must be `suspend` functions.
- Error handling strategy must be explicitly declared (`onError().log()` etc.).
- No stream injection as constructor parameters — use declarative input sources.
- No manual `cancelOnStop()` — lifecycle is automatic via the subscription engine.


## Known Limitations (mid-development)
- **Daily scheduling** (`onEveryDay`) — not yet implemented.
- **Queue processing** (`onQueue`) — stub only.
- **Scheduled tasks** (`onScheduledTask`) — stub only.
- **`mapBeforeDispatch`** — throws TODO on domain input sources.
- **Job system** — API defined (`RaptorJob`, `RaptorJobCommand`, `RaptorJobChange`), runtime not wired.


## Dependencies
- `raptor-core`, `raptor-di`, `raptor-domain`, `raptor-event`, `raptor-lifecycle`
- `fluid-time`, `kotlinx-coroutines-core`, `kotlinx-serialization-core`
