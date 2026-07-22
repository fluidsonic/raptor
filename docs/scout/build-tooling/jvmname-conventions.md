# `@file:JvmName("...@graph")` — the `@`-suffix workaround

Files in the `graphql` module carry a `@file:JvmName` whose value contains a literal
`@` (e.g. `"Utility@graph"`, `"LocalDate@graph"`, `"Country@graphql"`). This looks like a
typo or cosmetic boilerplate — it is neither. Do not "clean it up" or normalize the `@`.

Every affected file lives in the root package `io.fluidsonic.raptor` and links JetBrains
issue **KT-12495** in a top-of-file comment as the reason. The `@` is illegal in a Kotlin
identifier but legal in a JVM class name, so the suffix renames the generated file-facade
class. The KT-12495 comment marks it as a deliberate workaround — leave it in place.

Verifiable contrast: `modules/graph/` declares the same `Companion.graphDefinition()`
extensions in identically named files (LocalDate.kt, etc.) yet needs **no** `@JvmName`,
because that module uses sub-package `io.fluidsonic.raptor.graph`; only `graphql`, which
uses the root package, carries the suffix. A new scalar/extension file in `graphql`
should copy the header.

The suffix is applied inconsistently: only `Country.kt` and `CountryCode.kt` use
`@graphql`; every other file uses `@graph`. Harmless but real.

Anchors: `modules/graphql/sources-jvm/utility/Utility.kt`,
`modules/graphql/sources-jvm/extensions/` (Boolean.kt, Country.kt, LocalDate.kt, Url.kt…).

Separately, in `modules/graphql/sources-jvm/graphql/RaptorTypeNodeExtensionKey.kt`, the
generic `GNodeExtensionSet.Builder<T>` receiver `raptorType` property (for
`GArgumentDefinition` vs `GFieldDefinition`) carries explicit `@JvmName` on its
getter/setter (`getForArgumentDefinition`…) to avoid a platform-declaration clash — also
load-bearing, not decorative.
