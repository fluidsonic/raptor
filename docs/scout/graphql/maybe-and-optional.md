# `Maybe<T>` encodes GraphQL argument optionality (absent vs null)

Raptor distinguishes three argument states — required, nullable, and truly-omittable — and
the last is expressed by wrapping the Kotlin type in `Maybe<T>` (fluid-stdlib). This is a
convention enforced only by runtime checks and spread across several files.

- **Declaration side.** `RaptorGraphArgumentDefinitionBuilder` sets `isMaybe =
  kotlinType.classifier == Maybe::class`. A `Maybe` argument may **not** have a default: any
  `default*(...)` call throws "An optional argument of type '...' cannot have a default
  value." So `Maybe` (omittable) and defaults are mutually exclusive; nullability
  (optionally with `defaultNull`) is the separate "present but may be null" state.
- **Schema side.** `GraphSystemBuilder.typeRef`/`underlyingType` unwrap `Maybe<T>` to `T`'s
  ref via an early `return` that bypasses the nullability wrapper, so a `Maybe<T>` argument
  always produces a **nullable** GraphQL type and `Maybe` never appears as its own type.
  `GraphTypeSystemBuilder.directivesForArgument` attaches the synthetic `@optional` directive
  to any `Maybe`-typed argument. The `@optional` directive *definition* is emitted only when
  some argument references it (`findReferencedDirectiveNames`), so it appears/disappears from
  the schema based on presence of Maybe args. `GSchema` is built with `supportOptional = true`.
- **Runtime coercion (two stages that treat an explicit null differently).**
  `NodeInputCoercer.coerceNodeInput` collapses a *null node input* to `Maybe.nothing` for a
  `Maybe`-typed argument (everything else falls through to `next()`). `ArgumentResolver.resolve`
  then reads `argumentValues`: an **absent** key → `Maybe.nothing`; a value already equal to
  `Maybe.nothing` (as set by the coercer) is preserved; any other present value — including a
  plain `null` — is wrapped as `Maybe.of(value)`. Net contract: an **omitted** argument →
  `Maybe.nothing`, whereas a **present-but-null** argument that reaches the resolver as a plain
  `null` → `Maybe.of(null)`.

Anchors (all in the current `modules/graph`; near-identical code exists in the deprecated
`modules/graphql` — see `two-implementations.md`):
`modules/graph/sources-jvm/definitions/RaptorGraphArgumentDefinitionBuilder.kt` (`isMaybe`),
`modules/graph/sources-jvm/graphql/GraphSystemBuilder.kt` (`typeRef`, `underlyingType`, `buildDirectiveDefinitions`, `findReferencedDirectiveNames`),
`modules/graph/sources-jvm/graphql/GraphTypeSystemBuilder.kt` (`directivesForArgument`),
`modules/graph/sources-jvm/graphql/coercion/NodeInputCoercer.kt`,
`modules/graph/sources-jvm/graphql/resolution/ArgumentResolver.kt` (`resolve`).
</content>
