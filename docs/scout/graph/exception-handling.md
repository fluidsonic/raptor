# GraphQL exception handling: handler matching and message exposure

How thrown exceptions become client-facing GraphQL errors in `modules/graph`. Handler:
`modules/graph/sources-jvm/graphql/execution/ExceptionHandler.kt` (`handleException`),
flagged `// FIXME Rework exception handling.` — this FIXME is only on this class.

- **`GErrorException` bypasses handling** — rethrown unchanged, the escape hatch to surface
  a specific `GError` straight to the client.
- **Unhandled exceptions yield a generic error, never developer detail.** With no matching
  handler the exception is logged (a `Logger` from DI via `context.di.getOrNull<Logger>()`,
  else `System.err`) and the client gets a fixed `internalError` ("An internal error
  occurred.", extensions `{code: internal}`). Surfacing anything specific requires a
  registered handler.
- **A handler that throws is re-handled recursively** up to depth 100 ("GraphQL exception
  handlers caused a cycle."), the original chained via `addSuppressed`.
- **Nearest-match walks superclasses only, never interfaces** (`handler`, `closest`/
  `distance`). A handler registered on an exception *interface* works only when it is the
  sole `isInstance` match; with 2+ matches `closest()` cannot locate the interface up the
  `superclass` chain and fails. **Register handlers on concrete classes.**

**No handler is pre-registered.** `RaptorGraphComponent`
(`modules/graph/sources-jvm/assembly/RaptorGraphComponent.kt`) registers none, so until an
application calls `handle<…>` every failing resolver yields that generic internal error. Input
rejections are the exception that proves the first rule: `invalid(…)` throws a ready-made
`GErrorException` and reaches the client with its own `extensions` (`invalid-input-errors.md`) —
which is also why registering a handler for them is pointless.

Trap: `modules/ktor-graph/sources-jvm/graphql/execution/ExceptionHandler.kt` is an unreferenced
second `GExceptionHandler` that maps any non-`ServerFailure` to `ServerFailure.internal` and
puts `failure.developerMessage` into the client `message`. Nothing wires it up — do not, it
leaks internals the handler above deliberately hides.
