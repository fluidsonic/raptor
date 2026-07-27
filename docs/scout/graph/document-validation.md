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
  with no rewriting, filtering, deduplication or reordering. The message text, order and count
  of validation errors that clients receive are fluid GraphQL's, unfiltered — an upstream
  release that changes them changes raptor's HTTP responses (still HTTP 200, see
  `../ktor-graph/route-wiring.md`).
- **Only executable documents are validated.** The schema is assembled programmatically from
  `GNamedType` nodes (`GraphSystemBuilder.buildTypeDefinitions` in
  `modules/graph/sources-jvm/graphql/GraphSystemBuilder.kt`); no API accepts SDL or a
  caller-supplied `GSchema`, so upstream SDL validation changes are unobservable here.
- **Test-harness symptom:** `GraphFixture.execute` (`modules/graph/tests-jvm/GraphTestSupport.kt`)
  wraps `graph.parse` in `checkNotNull`, so a newly-firing validation rule fails tests with
  "cannot parse or validate query", not with an `errors` entry.

Only the `graphql-execution` artifact is consumed, by `modules/graph/build.gradle.kts` and
`modules/ktor-graph/build.gradle.kts`, both through the single `Versions.fluid_graphql`
constant. version: io.fluidsonic.graphql 0.18.0 (as of 2026-07-27;
`buildSrc/sources/Versions.kt`)
