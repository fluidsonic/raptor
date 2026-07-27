# GraphQL's built-in scalars are mapped but never declared

Raptor binds Kotlin `Boolean`, `Double`, `Int` and `String` to GraphQL's built-in scalars
without emitting a scalar type for them. Know this before adding or "fixing" a default scalar.

- The four mappings come from the internal `graphUncoercedScalarDefinition`
  (`modules/graph/sources-jvm/definitions/RaptorGraphDefinitionDsl.kt`) — the only way to build
  a scalar whose `parse` and `serialize` are both `null`; the public `graphScalarDefinition`
  always coerces (`RaptorScalarGraphDefinitionBuilder.build` errors without both).
  `ScalarGraphType.hasCoercer`
  (`modules/graph/sources-jvm/graphql/types/GraphType.kt`) exposes that state and
  `GraphSystemBuilder.isDeclaredByFluid` filters exactly those types out of the emitted
  document. They stay in raptor's type system because that is what carries the Kotlin-type
  binding used to resolve type references.
- Why they must not be declared: fluid GraphQL appends its own built-in singletons after the
  supplied types, so it wins every by-name lookup — a raptor scalar named `Int` is inert and
  its coercer never runs — while the raptor declaration still reaches the emitted document,
  leaving a duplicate name in `GSchema.types` and an invalid `scalar Int` line in the served
  SDL. Regression guard: `modules/graph/tests-jvm/SchemaShapeTests.kt`
  (`schemaDeclaresNoCustomScalarsForBuiltInNames` and the `sdlOmits…` tests).
- `Double` is mapped under the explicit name `Float`
  (`modules/graph/sources-jvm/extensions/Double.kt`).
- There is no raptor type for `ID` at all — no Kotlin type maps to it by default. Expose one as
  an ID alias (`graphIdAliasDefinition` / `definitions.newIdAlias`) — see `alias-types.md`.

version: io.fluidsonic.graphql 0.18.0 (as of 2026-07-27; `Versions.fluid_graphql` in
`buildSrc/sources/Versions.kt`)
