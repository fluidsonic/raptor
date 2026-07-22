# Argument resolution: property-name binding, ThreadLocal coroutine trap

How GraphQL arguments get their names and values — with a live coroutine footgun.
Anchors point at the current `modules/graph` module (see `two-implementations.md`).

- **An argument's GraphQL name defaults to the Kotlin property it's bound to.** The name is
  finalized in `provideDelegate`: the explicit builder `name(...)` if set, otherwise
  `property.name`. So `val locale by argument<Locale> { }` silently exposes a schema argument
  named `locale`. Two names are tracked: `name` (GraphQL) and `variableName` (always the
  property name) — they can differ. Binding one argument to two properties throws
  ("Cannot delegate multiple variables to the same argument"). The definition *implements*
  `RaptorGraphArgumentDelegate`, so `map`/`validate` mutate it directly (appending to a
  `transforms` list, TODO "freeze after builder is done"), while `provideDelegate` hands back
  a separate `ReadOnlyProperty`. Anchor:
  `modules/graph/sources-jvm/definitions/RaptorGraphDefinition.kt` (`GraphArgumentDefinition`).
- **`ArgumentResolver` passes per-call state via a `ThreadLocal` — it breaks under
  coroutines.** The `Context` is set in `withArguments { }` and read by `resolveArgument`.
  Delegated arguments only resolve while executing synchronously on the same thread. Since
  `FieldResolver.resolveField` is `suspend` and wraps the field's `resolve(...)` in
  `withArguments`, any suspension that resumes on a different dispatcher thread before the
  resolver touches its delegated arguments loses the context and throws "can only be accessed
  within '<factory> { … }'". The `currentContext` ThreadLocal carries an explicit
  `// TODO won't work with coroutines`. Anchors:
  `modules/graph/sources-jvm/graphql/resolution/ArgumentResolver.kt` (`currentContext`,
  `resolveArgument`), `.../resolution/FieldResolver.kt` (`resolveField`).
- **`invalid(details)` in the argument DSL surfaces as a client-facing error, not a 500.**
  The `Context.resolve` `invalid()` (and the operation-builder resolver) call
  `invalidValueError(...)`, throwing `InvalidValueException`; `RaptorGraphComponent`'s init
  registers a default handler for it emitting a GraphQL error with `code: "invalid value"`.
  The Scalar/Enum/InputObjectCoercer `invalid()` instead delegates to the io.fluidsonic.graphql
  coercer context (a GraphQL input-coercion error) — both are client-facing input errors.
  (The legacy `graphql` module still wires this to `error(...)`; do not carry that over.)
  Anchors: `modules/graph/sources-jvm/exceptions/InvalidValueException.kt` (`invalidValueError`),
  `.../assembly/RaptorGraphComponent.kt` (init `handle<InvalidValueException>`).

Field-builder split: interface / interface-extension builders emit `Unresolvable` fields
(no resolver); object / object-extension builders use `RaptorGraphFieldBuilder.WithResolver`.
Duplicate field/argument detection uses reference equality (`it.name === name`), which only
catches interned/same-instance strings — a latent trap.
</content>
