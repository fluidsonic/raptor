# Schema generation: alphabetical sorting, default scalars, empty-object workaround

Non-obvious rules baked into the schema builder that surprise anyone expecting
declaration-order output or a richer default scalar set. The GraphQL implementation lives in
`modules/graph/` (package `io.fluidsonic.raptor.graph`); HTTP wiring lives in
`modules/ktor-graph/` (see `../ktor-graph/route-wiring.md`).

- **The whole schema is alphabetized.** `modules/graph/sources-jvm/graphql/GraphSystemBuilder.kt`
  runs nearly every collection through `.sortedBy { it.name }` / `.sorted()` before emitting:
  top-level type definitions, each type's fields, each field's arguments, enum values,
  implemented interfaces, and union possible types. Generated SDL field/argument order does
  **not** match Kotlin declaration order. The sorting is scattered across the `build*` methods
  with no explanatory comment.
- **Only the four built-in-scalar mappings are auto-registered.**
  `GraphSystemDefinitionBuilder.defaultDefinitions`
  (`modules/graph/sources-jvm/graphql/GraphSystemDefinitionBuilder.kt`) wires in exactly
  `Boolean`, `Double`, `Int`, `String` before the user's definitions — and none of them reach
  the emitted schema, see `builtin-scalars.md`. Every other scalar (`Country`, `CountryCode`,
  `Currency`, `Duration`, `LocalDate`, `LocalDateTime`, `LocalTime`, `Locale`, `Timestamp`,
  `TimeZone`, `Unit`) is an opt-in `X.Companion.graphDefinition()` — added individually or in
  bulk via `Definitions.includeDefault()`, which registers `RaptorGraphDefaults.definitions`
  and is idempotent through an `includesDefault` flag
  (`modules/graph/sources-jvm/assembly/RaptorGraphComponent.kt`). Omitting one causes a
  missing-type error, not a compile error.
- **Empty Kotlin `object` types get a synthetic `_` field.**
  `RaptorObjectGraphDefinitionBuilder.build` (`modules/graph/sources-jvm/definitions/RaptorObjectGraphDefinitionBuilder.kt`)
  forks on `kotlinType.classifier.objectInstance`: a singleton `object` with no fields gets
  an injected `_` field of type `Unit` (description cites `graphql/graphql-spec#568`); a
  non-object class with no fields errors "At least one field must be defined."
- **Reserved names & no subscriptions.** Names are guarded in two places:
  `registerNamedTypeDefinition` (same `GraphSystemDefinitionBuilder.kt`) errors if a type name
  equals the default Query/Mutation type name (the Subscription check is commented out), and
  `GraphSystemBuilder.checkTypeNameIsNotReserved` errors on GraphQL's reserved type names — see
  `schema-assembly-checks.md`. `GraphTypeSystemBuilder.buildType`
  (`modules/graph/sources-jvm/graphql/GraphTypeSystemBuilder.kt`) has an exhaustive `when`
  over `RaptorGraphOperationType` with only `query` and `mutation` arms — **subscriptions are
  not implemented.** Query/Mutation roots are backed by empty private placeholder `object`s
  (`MutationRoot`, `QueryRoot`) used only as distinct Kotlin classifiers.

Definition-site diagnostics: every builder captures `stackTrace(skipCount = 1)` and the
`RaptorGraphNode.toString()` (`modules/graph/sources-jvm/definitions/RaptorGraphDefinition.kt`)
appends it, so schema errors point back to the user's DSL call site — but `nested{}` builders
build lazily and lose that site (documented TODO in
`modules/graph/sources-jvm/definitions/RaptorStructuredGraphTypeDefinitionBuilder.kt`).
