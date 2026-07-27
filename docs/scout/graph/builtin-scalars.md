# GraphQL's built-in scalars are mapped by name only, never declared

Raptor binds Kotlin `Boolean`, `Double`, `Int` and `String` to GraphQL's built-in scalars without
holding a type of its own for them. Know this before adding or "fixing" a default scalar.

- The four mappings come from the internal `graphUncoercedScalarDefinition`
  (`modules/graph/sources-jvm/definitions/RaptorGraphDefinitionDsl.kt`) — the only way to get a
  scalar definition whose `parse` and `serialize` are both `null`; the public
  `graphScalarDefinition` always coerces (`RaptorScalarGraphDefinitionBuilder.build` errors without
  both). Such a definition never becomes a `ScalarGraphType`: `GraphTypeSystemBuilder.build` puts it
  into a Kotlin-type → name map, read back by `GraphTypeSystem.resolveExternallyDeclaredTypeName` as
  `GraphSystemBuilder.typeRef`'s last resort when no raptor type resolves.
- **`GraphTypeSystem.types` is therefore exactly what raptor emits**, so `buildTypeDefinitions`
  needs no filtering — but these Kotlin types have no `GraphType` to hang anything off; see
  `alias-types.md` for what that costs the `raptorType` node extension. Redeclaring one of the five
  names is not silently shadowed either: assembly fails, see `schema-assembly-checks.md`.
- **Coercing these four is fluid GraphQL's job, and its `Float` rejects non-finite values.** A
  resolver returning `Double.NaN` or an infinity fails output coercion, so the client gets an error
  entry and, the field being non-null, `data: null`. Guard: `nonFiniteFloatOutputProducesClientError`
  in `modules/graph/tests-jvm/BuiltinScalarCoercionTests.kt`.
- `GSchema.types` lists a built-in scalar **only when some definition references it** — `Boolean`
  and `String` always appear, because the built-in directives take arguments of those types.
  `printSchema` emits no `scalar` line for a built-in either way. Guards in
  `modules/graph/tests-jvm/SchemaShapeTests.kt`: `schemaDeclaresNoCustomScalarsForBuiltInNames` and
  the `sdlOmits…` tests, whose fixture names all five so `Float`, `ID` and `Int` are listed too.
- `Double` is mapped under the explicit name `Float`
  (`modules/graph/sources-jvm/extensions/Double.kt`). Nothing maps to `ID`; expose one as an ID alias
  (`graphIdAliasDefinition` / `definitions.newIdAlias`) — see `alias-types.md`.

version: io.fluidsonic.graphql 0.19.0 (as of 2026-07-28; `Versions.fluid_graphql` in
`buildSrc/sources/Versions.kt`)
