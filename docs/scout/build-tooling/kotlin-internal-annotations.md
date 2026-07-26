# Reliance on `kotlin.internal` annotations across DSLs

A repo-wide, deliberate-but-fragile technique: most builder/DSL entrypoints depend on
undocumented compiler-internal annotations. Recognize the pattern before "cleaning up"
suppressions.

Files that pin a reified/`KType` type parameter for a configure lambda open with
`@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")` and `import kotlin.internal.*`,
then annotate the type parameter with `@NoInfer` (to force the caller's explicit type
rather than inferring it from a value/lambda) and/or `@LowPriorityInOverloadResolution`
(to disambiguate from a deprecated or value-taking overload). Removing the suppressions
will not compile.

Evidence-backed motivations cited in inline comments:
- BSON definition DSL — `RaptorBsonDsl.kt` (the `definition` entrypoint) cites
  JetBrains **KT-54477** (`@NoInfer` doesn't work for builders); the matching
  `RaptorBsonDefinitionBuilder.kt` uses the same `@NoInfer` /
  `@LowPriorityInOverloadResolution` pattern without re-citing the ticket.
- DI DSL — `modules/di/sources-jvm/assembly/RaptorDIComponent.kt` (`provide`/
  `provideOptional`) cite **KT-54478** (`@NoInfer` causing `CONFLICTING_OVERLOADS`),
  the reason for `@LowPriorityInOverloadResolution` on the value-taking overloads.
- Also used in: `RaptorBsonComponent.kt` (`definition`), `RaptorComponentRegistry.kt`
  (`register` vs a deprecated lambda overload), DI key factories
  (`KTypeDIKey.kt`, `LabeledDIKey.kt`), and the graph DSL
  (`RaptorGraphComponent.kt` `newEnum`/`newObject`/…, `RaptorGraphDefinitionDsl.kt`).

A related, load-bearing workaround: in
`modules/graph/sources-jvm/graphql/RaptorTypeNodeExtensionKey.kt` the two `raptorType`
extension properties (for `GArgumentDefinition` and `GFieldDefinition` builders) carry explicit
`@JvmName`s on their getters/setters to avoid a platform-declaration clash — not decorative.
