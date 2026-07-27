# Rejected input: `extensions.code` is the contract, `path` is not

What a client sees when `invalid(…)` rejects a value in a scalar/alias `parse`, an input-object
factory or an argument transform — and which parts of that payload are safe to rely on.

- **The error carries its own `extensions`; no exception handler is involved.**
  `GraphInputScope.invalid` (`modules/graph/sources-jvm/graphql/coercion/GraphInputScope.kt`)
  throws a `GErrorException` whose `GError` already holds
  `extensions = mapOf("code" to "invalid value")`. This has to travel on the error itself because a
  literal rejected by an attached scalar coercer is raised during *document validation* (see
  `document-validation.md`), a phase with no executor exception handler — an earlier design put the
  code on a pre-registered handler and literals silently lost it. `ExceptionHandler` rethrows a
  `GErrorException` untouched (`exception-handling.md`), so the same code reaches the client from
  execution too.
- **All six rejection sites carry that code** — scalar, input-object factory and argument
  transform, each for a literal and for a variable. Pinned by `executeExpectingClientErrors`
  (`modules/graph/tests-jvm/GraphTestSupport.kt`) in `InvalidInputTests.kt`,
  `InputObjectFactoryTests.kt` and `ArgumentTransformTests.kt`; the message wording is deliberately
  never asserted, only printed.
- **Do not expect a `path`, and never expect a variable name in one.** `path` identifies a
  *response field*, which a variable position is not, so a rejection during variable coercion is a
  request error: no `path`, and no `data` key at all. Only a rejection raised while a field is
  being resolved carries one — an argument transform always does, an input-object factory does for
  a literal — and those responses carry `data: null` instead. A scalar literal, rejected one phase
  earlier during validation, carries the offending value's document `locations`.

Related: `coercion-scopes.md`.
version: io.fluidsonic.graphql 0.19.0 (as of 2026-07-28; `Versions.fluid_graphql` in
`buildSrc/sources/Versions.kt`)
