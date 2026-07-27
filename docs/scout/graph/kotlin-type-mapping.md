# Kotlin-to-GraphQL type mapping: names, `Set`, generics, no `Maybe`

How a Kotlin type becomes a GraphQL type reference. `KotlinType`
(`modules/graph/sources-jvm/graphql/KotlinType.kt`) is a hand-rolled reflection wrapper, *not*
`kotlin.reflect.KType`, and its rules only bite at assembly time.

- **A type's default GraphQL name is `classifier.simpleName`.** `KType.defaultGraphName()`
  (`modules/graph/sources-jvm/extensions/KType.kt`) hard-errors for any non-`KClass` classifier
  (e.g. a type parameter), telling the caller to pass `name = …`. That explicit override is
  what makes Kotlin `Double` appear as `Float` (see `builtin-scalars.md`).
- **Kotlin `Set` maps to a GraphQL list** — `Set::class` is handled alongside `Collection` and
  `List` in `GraphSystemBuilder.typeRef` — so uniqueness semantics are lost.
- **`Any::class` is the sentinel for "totally generic".** The public `KotlinType.of(…)` never
  returns null: where the private overload gives up, it falls back to
  `KotlinType(classifier = Any::class)` — the commented-out `?: error(…)` right above it is the
  road not taken (comment "so many hacks… basically means totally generic type"). `isGeneric` is true for it,
  and the argument-less `specialize()` refuses it ("Cannot specialize this type."). Generic
  types are specialized lazily on demand, which is why
  `GraphSystemDefinitionBuilder.resolveAllReferences` is genuinely recursive (declared
  `tailrec`) — resolving one reference can register new definitions. A definition that reaches
  `GraphTypeSystemBuilder.buildType` unspecialized fails with "Definition has not been
  specialized", and generic object types are excluded from union possible types (`unions.md`).
- **An unmapped Kotlin type fails at one of two stages, with two different messages.** As an
  operation's own output type, `GraphSystemDefinitionBuilder.resolveReferences` rejects it ("Cannot
  resolve output type of …"); on a field or an input-object argument `TypeDefinitionRegistry.resolve`
  silently returns `null` and the failure only surfaces in `GraphSystemBuilder.typeRef` ("Cannot
  resolve GraphQL type for Kotlin type '…'."). To exercise the second, reference the type from a
  field — see `unmappedKotlinTypeIsRejected` in `modules/graph/tests-jvm/SchemaShapeTests.kt`.
- **`Maybe<T>` is rejected outright** ("'Maybe' is not supported by the GraphQL type mapping").
  Argument absence is not representable; use a nullable argument, plus `defaultNull()` if a
  default is wanted.
