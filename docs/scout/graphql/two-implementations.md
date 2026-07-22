# Two parallel GraphQL implementations — `graph` is current, `graphql` is deprecated

Read this first before touching any GraphQL code: there are two near-identical
implementations and it is easy to edit the wrong one.

- **`modules/graph`** — the current implementation. Package `io.fluidsonic.raptor.graph`.
  Contains `RaptorGraphOperation`, `RaptorGraphOperationBuilder`, `DefaultRaptorGraph`, and
  its own `RaptorGraphDefinition`/`UnionGraphDefinition`/argument-builder set.
- **`modules/graphql`** — the legacy copy being replaced. Its `build.gradle.kts` is
  annotated `// TODO Deprecated module. Delete.` Packages `io.fluidsonic.raptor` and
  `io.fluidsonic.raptor.graphql.internal`; defines its OWN sealed `RaptorGraphDefinition`
  hierarchy (its definition classes are `internal`), argument builders, `RaptorGraphDefaults`,
  and a Ktor route plugin.

The two modules **share type names** (`RaptorGraphDefinition`,
`RaptorGraphArgumentDefinitionBuilder`, `UnionGraphDefinition`, `GraphSystemBuilder`,
`ArgumentResolver`, `ExceptionHandler`, …) but they are **distinct types in different
packages** — code and knowledge from one does not transfer to the other. Anchors:
`modules/graph/sources-jvm/operations/RaptorGraphOperation.kt`,
`modules/graphql/sources-jvm/definitions/RaptorGraphDefinition.kt`,
`modules/graphql/build.gradle.kts`.

Note: `modules/ktor-graph` (current) vs the deprecated `graphql` module's built-in Ktor
route plugin are likewise parallel — new HTTP wiring lives in `ktor-graph`. See
`ktor-graph/route-wiring.md`.

The other GraphQL leaves in this folder describe behavior that exists (with near-identical
code) in **both** modules unless stated; anchors point at `modules/graph` as the surviving
copy.
</content>
