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

Every graph auto-registers (in `RaptorGraphComponent`'s init block,
`modules/graph/sources-jvm/assembly/RaptorGraphComponent.kt`) a handler for
`InvalidValueException` → code `"invalid value"`. Subclasses (`TooLong`/`TooShort`/
`TooLarge`/`TooSmall`) assert in their constructor that the value violates the bound
(`ForbiddenCharacter` asserts its index is in range) — a non-violating value throws
`IllegalArgumentException`. Anchor:
`modules/graph/sources-jvm/exceptions/InvalidValueException.kt`.

Trap: `modules/ktor-graph/sources-jvm/graphql/execution/ExceptionHandler.kt` is an unreferenced
second `GExceptionHandler` that maps any non-`ServerFailure` to `ServerFailure.internal` and
puts `failure.developerMessage` into the client `message`. Nothing wires it up — do not, it
leaks internals the handler above deliberately hides.
