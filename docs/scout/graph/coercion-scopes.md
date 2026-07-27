# Coercion scopes: no transaction in `parse`/`serialize`

The scope a graph callback receives decides whether it can reach the transaction — and
therefore dependency injection. The three scopes look interchangeable but are not.

- `RaptorGraphInputScope` (a scalar's/enum's/alias' `parse`, an input object's `factory`, an
  argument's `map`/`validate`) and `RaptorGraphOutputScope` (`serialize`) carry **no**
  transaction: they are stateless, and the implementations are the internal singletons
  `GraphInputScope` and `GraphOutputScope`. There is no `context`, hence no `context.di`: a
  scalar parser cannot resolve a service — do that in the resolver instead. Only
  `RaptorGraphResolverScope` (field and operation `resolver { … }`) also implements
  `RaptorTransactionScope`. Anchors: `modules/graph/sources-jvm/graphql/coercion/`,
  `modules/graph/sources-jvm/graphql/resolution/RaptorGraphResolverScope.kt`.
- `invalid(details)` on the input scope throws a fluid `GErrorException` that already carries
  `extensions = {"code": "invalid value"}` (`GraphInputScope.invalid`); raptor has no exception type
  of its own for it, and nothing routes through fluid GraphQL's coercer context any more — those
  context types were removed in fluid-graphql 0.19.0, whose coercer interfaces
  (`GInputLiteralCoercer`, `GInputValueCoercer`, `GOutputValueCoercer`, implemented in
  `modules/graph/sources-jvm/graphql/coercion/`) take a plain value with no receiver. Literal and
  variable rejections both reach the client as an `errors` entry carrying that code — see
  `invalid-input-errors.md` for what differs.
- Coercers are **per-type instances**, constructed with their raptor type by
  `GraphSystemBuilder` (`ScalarCoercer`, `EnumCoercer`, `InputObjectCoercer`), not shared
  singletons that look a type up by name. One application may host several graphs whose type
  sets are disjoint (`modules/graph/tests-jvm/MultiGraphTests.kt`,
  `scalarsAreNotSharedBetweenGraphs`), so per-graph state must hang off the graph itself: in
  dependency injection the last-registered graph would silently win for all of them and the
  result be memoized (see `../di/resolution-semantics.md`).
