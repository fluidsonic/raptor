# Alias types are invisible in the schema

An alias (`graphAliasDefinition`, `graphIdAliasDefinition`, or `definitions.newIdAlias`)
maps a Kotlin type onto an already-mapped GraphQL type. Unlike every other definition it
produces no GraphQL type of its own, which changes what clients must write and where its
`parse`/`serialize` can run.

- `AliasGraphType` (`modules/graph/sources-jvm/graphql/types/GraphType.kt`) is not a
  `NamedGraphType` and has no name. `GraphSystemBuilder.typeRef` resolves an alias to
  `GIdTypeRef` when `isId`, otherwise to the referenced type's ref, so the alias name appears
  neither in the schema nor in introspection. **A query must declare variables with the
  referenced type** — `query($value: ID!)` for an ID alias over a value class; see
  `modules/graph/tests-jvm/IdAliasCoercionTests.kt` (`idAliasVariableInput`).
- With no type to attach a coercer to, the alias is instead carried on argument and field
  definitions through the `raptorType` node extension
  (`modules/graph/sources-jvm/graphql/RaptorTypeNodeExtensionKey.kt`, which offers that
  extension for `GArgumentDefinition` and `GFieldDefinition` only) and applied by
  `ArgumentResolver.resolve` via `parseAliasValue` for input and by `FieldResolver` via
  `serializeAliasValue` for output. Both walk list and non-null wrappers themselves.
- Consequence: conversion happens only where an argument or field definition carries that
  extension. Anywhere fluid GraphQL coerces by type — variable coercion in particular — the
  value is handled as the referenced type, and raptor converts it afterwards.
- **`raptorType` is nullable and both readers narrow it with `as? AliasGraphType`**
  (`ArgumentResolver`, `FieldResolver`). It is filled from `GraphSystemBuilder.underlyingType`,
  which returns `null` for a Kotlin type mapped onto a built-in scalar (`builtin-scalars.md`), so it
  reads back as `null` on an argument or field of type `Int` or `String`. A new reader that assumes
  a non-null `GraphType` there would break on exactly those.

Related: `builtin-scalars.md` (ID has no raptor scalar type), `coercion-scopes.md`.
