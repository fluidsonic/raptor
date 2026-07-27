# Document validation is raptor's own call, and its errors reach clients verbatim

Where GraphQL document validation happens in raptor, and why bumping `Versions.fluid_graphql`
can change what clients see without a single source change.

- **Validation lives in `parse`, not in `execute`.** `DefaultRaptorGraph.parse`
  (`modules/graph/sources-jvm/implementation/DefaultRaptorGraph.kt`) chains `GDocument.parse`
  into `document.validate(schema)` with no rule overrides (so fluid GraphQL's defaults apply)
  and turns a non-empty error list into a `GResult.failure`. `DefaultRaptorGraph.execute`
  takes an already-parsed `GDocument` and validates nothing itself, so a document obtained
  anywhere other than `RaptorGraph.parse` executes unvalidated.
- **Validation errors bypass raptor's exception handling entirely.** They are already a
  `GResult` failure, so they never enter `ExceptionHandler` (see `exception-handling.md`,
  which only sees resolver and coercion exceptions). `modules/ktor-graph/sources-jvm/graphql/GraphRoute.kt`
  (`executeDocument`) hands the failure straight to `graph.serialize` → `GExecutor.serializeResult`
  with no rewriting, filtering or reordering. The message text, order and count of validation
  errors clients receive are fluid GraphQL's — an upstream release that changes them changes
  raptor's HTTP responses (still HTTP 200, see `../ktor-graph/route-wiring.md`).
- **Only executable documents are validated.** The schema is assembled programmatically from
  `GNamedType` nodes (`GraphSystemBuilder.buildTypeDefinitions` in
  `modules/graph/sources-jvm/graphql/GraphSystemBuilder.kt`); no API accepts SDL or a
  caller-supplied `GSchema`, so upstream SDL validation changes are unobservable here.
- **Validation also runs raptor's own scalar coercers.** Fluid GraphQL validates a scalar
  literal by invoking the coercer raptor attached as `inputLiteralCoercer`, so a rejected
  literal argument is a *validation* failure — `parse` fails and no resolver runs, before the
  executor and its exception handling ever come into play. The rejection nevertheless reaches the
  client with raptor's `extensions.code` intact — fluid GraphQL reports the `GError` raptor threw
  instead of wrapping its message, as it did up to 0.18.0. See `invalid-input-errors.md`.
- **Test harness:** `GraphFixture.execute` (`modules/graph/tests-jvm/GraphTestSupport.kt`)
  flat-maps the `graph.parse` result into `graph.execute`, so a parse or validation failure is
  serialized into `errors` as the Ktor route does it, rather than throwing. A newly-firing
  validation rule shows up as an unexpected `errors` entry, failing `executeData`.

Only the `graphql-execution` artifact is consumed, by `modules/graph/build.gradle.kts` and
`modules/ktor-graph/build.gradle.kts`, both through the single `Versions.fluid_graphql`
constant. version: io.fluidsonic.graphql 0.19.0 (as of 2026-07-28;
`buildSrc/sources/Versions.kt`)
