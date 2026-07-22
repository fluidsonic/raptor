# GraphQL HTTP route wiring (ktor-graph)

The current HTTP surface for GraphQL. `modules/ktor-graph` is the live module; the
deprecated `graphql` module has a parallel copy (see `graphql/two-implementations.md`).

- **`graph()` and `graphSchema()` are mutually exclusive per Ktor route.** Both DSL
  functions guard installation using the *same* extension key `Keys.graphInstalledExtension`
  (check `extensions[graphInstalledExtension] != true`). You cannot install two graph
  handlers, two schema handlers, or one of each in the same route — the second throws
  "Cannot install multiple ... or mix it with ... in the same Ktor route." Anchors:
  `modules/ktor-graph/sources-jvm/assembly/Keys.kt`, `RaptorGraphKtorRoutePlugin.kt` (`graph`),
  `RaptorGraphSchemaKtorRoutePlugin.kt` (`graphSchema`).
- **HTTP methods:** the graph query route registers both `get` and `post` dispatching to
  `GraphRoute.handle(call)`; the schema route registers only `get` responding with
  `schema.toString()` as `text/plain`.
- **Two-phase route plugin lifecycle.** The `get`/`post` handlers are claimed in
  `onConfigurationStarted`, but the actual `GraphRoute` / `GSchema` property (resolved via
  `require(RaptorGraphPlugin).taggedGraph(tag)`) is registered into `propertyRegistry` only
  in `onConfigurationEnded`; handlers read it lazily at request time with `checkNotNull`.
- **`RaptorKtorGraphPlugin` is a require-only glue plugin** — its `install` does nothing but
  `require(RaptorGraphPlugin)` and `require(RaptorKtorPlugin)`; it registers no components.

**Request parsing and the transport-error vs GraphQL-error contract in `GraphRoute`.**
Parsing is defensive and never throws: `parseGraphRequest` returns a `GraphRequestParseResult`
(`Parsed` or `Bad`), and `handle` turns a `Bad` into a 4xx carrying a GraphQL-shaped body
`{"errors":[{"message":…}]}` via `respondGraphError`. Branch order: an `application/graphql`
Content-Type means the whole body is the query (POST only; a GET here returns 405); else a GET
reads `query`/`operationName`/`variables` from the query string; else the body is parsed as a
JSON object. Malformed or empty JSON, a missing or non-string `query`, a non-string
`operationName`, or non-object `variables` all become 400. Over GET, a mutation or
subscription operation returns 405 (`resolveGraphOperation` + `GOperationType`). The contract:
only transport-level failures (a request that cannot form a GraphQL response) are 4xx; once the
document parses, GraphQL parse/validation/execution errors return **HTTP 200** with an `errors`
array — `executeDocument` serializes every `GResult` failure via `graph.serialize` instead of
throwing. There is still no StatusPages plugin, so any *unexpected* exception escapes as a 500.
Anchors: `modules/ktor-graph/sources-jvm/graphql/GraphRoute.kt` (`parseGraphRequest`,
`resolveGraphOperation`, `GraphRoute.handle`, `executeDocument`).

For a non-null `tag` (and a non-empty graph set), `taggedGraph(tag)` resolves via
`graphs.singleOrNull { it.tags.contains(tag) }`, so BOTH the no-match and multi-match cases
fall through the same `?:` and report the misleading "There are multiple graphs but exactly
one was expected with tag: $tag". Anchor:
`modules/graph/sources-jvm/api/RaptorGraphPluginConfiguration.kt` (`taggedGraph`).
</content>
