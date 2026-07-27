# Schema validity is enforced at assembly, and every graph needs a query operation

What `raptor { graphs.new { … } }` rejects before the application starts, and why the errors
come from raptor rather than from fluid GraphQL.

`GraphSystemBuilder.buildSchema` (`modules/graph/sources-jvm/graphql/GraphSystemBuilder.kt`)
runs four gates in order: `buildTypeDefinitions` (which screens every type name, below), then
`checkQueryRootTypeIsProvided`, then the `GSchema` factory — which throws a `GErrorException` of its
own, for example for a redeclared built-in scalar — and finally `schema.assertValid()`. Raptor's own
two checks run first so the diagnostic is raptor's rather than fluid GraphQL's; both
`QueryRootTests` and `ReservedTypeNameTests` pin that by asserting the thrown exception is *not* a
`GErrorException`. Failing at assembly matters
because fluid GraphQL's `DefaultExecutor.execute` calls `assertValid()` *outside* its catch: an
invalid schema that reaches runtime kills the first request with an uncaught `GErrorException`
rather than returning a GraphQL error response.

- **A graph without a query operation no longer assembles.** Root operation types come solely
  from the operation definitions a graph declares (`GraphTypeSystemBuilder.build`), so an empty
  `graphs.new()` used to produce a `Query`-less schema. Object extensions cannot fill the gap —
  the root-type overload of `buildType` never consults the object-extension map — and a user
  type named `Query` is rejected by `registerNamedTypeDefinition` in
  `modules/graph/sources-jvm/graphql/GraphSystemDefinitionBuilder.kt`. Tests therefore attach a
  throwaway `hello` query (the `addHelloQuery` helper in
  `modules/graph/tests-jvm/AssemblyTests.kt`). Guard:
  `modules/graph/tests-jvm/QueryRootTests.kt`.
- **Reserved names** (`GLanguage.isReservedTypeName`: the five built-in scalar names and any
  `__` prefix) are checked by `checkTypeNameIsNotReserved`, applied by `buildTypeDefinitions` to
  every type the type system holds — no exemption needed, because raptor's own mappings for the
  built-in scalar names are not types at all (`builtin-scalars.md`). `ID` was the unguarded name.
  This stage holds only a `NamedGraphType`, so its message names the Kotlin type instead of the DSL
  call site. Guard: `modules/graph/tests-jvm/ReservedTypeNameTests.kt`.

version: io.fluidsonic.graphql 0.19.0 (as of 2026-07-28; `Versions.fluid_graphql` in
`buildSrc/sources/Versions.kt`)
