# `RaptorUnion2` — auto-generated union type + resolve-time unwrap

Returning or exposing a `RaptorUnion2<A, B>` triggers hidden schema generation and value
rewriting in the `raptor-graph` module. The declared Kotlin signature gives no hint of either.

- **Object field of union type.** When an object field's Kotlin type classifier is
  `RaptorUnion2::class`, `modules/graph/sources-jvm/definitions/RaptorObjectGraphDefinitionBuilder.kt`
  (the `field` builder) silently appends a `RaptorUnionGraphDefinitionBuilder` to
  `nestedDefinitions` named `"${objectName}_${fieldName}"`, and
  `modules/graph/sources-jvm/definitions/RaptorGraphFieldBuilder.kt` (`WithResolver.build`)
  wraps the resolver to call `.value` on the returned `RaptorUnion2`
  (`resolve(parent)?.cast<RaptorUnion2<*,*>>()?.value`). The user never declares the union nor
  unwraps it.
- **Operation output of union type.** When an operation's output classifier is
  `RaptorUnion2`, `modules/graph/sources-jvm/operations/RaptorGraphOperationBuilder.kt` (`build`)
  auto-appends a `UnionGraphDefinition` named `"<Operation>Output"` (via `defaultOutputTypeName`,
  even without an explicit `outputUnion {}` block), and the generated resolver detects
  `output is RaptorUnion2<*,*>` and returns `output.value`. `RaptorUnion2.T1/T2` encode the arms
  with `Nothing` in the unused slot. Both steps carry `// TODO hack` comments.

Union possible-types resolution in `modules/graph/sources-jvm/graphql/GraphSystemBuilder.kt`
(`resolvePossibleTypesForKotlinType`) has two strategies: for `RaptorUnion2::class` it reads the
two type arguments directly (comment "This is a hack."); for any other union classifier it
**scans all registered `ObjectGraphType`s at build time**, drops generic ones, and keeps those
whose Kotlin classifier is a subclass of the union — erroring if none match. So general union
membership is by runtime subclass scanning, not explicit declaration.

Note: a near-identical, unused copy of this logic lives in the legacy `modules/graphql/` module
(no consumers); the active module is `modules/graph/`.
