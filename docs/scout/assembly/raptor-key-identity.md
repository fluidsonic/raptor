# RaptorKey uses reference identity, not label equality

A silent-lookup-miss footgun that governs every property/extension/component key in the framework.

`RaptorKey` (`modules/core/sources-jvm/RaptorKey.kt`) declares `final override equals` as
`this === other` and `hashCode` as `super.hashCode()` (identity hash). The `label` string
participates **only** in `toString()`. Two `RaptorKey` instances with the same label are
never equal.

Because `DefaultRaptorKeyValueSet` and the property/extension registries store values in a
`Map<RaptorKey<*>, Any>` keyed by these instances, you must reuse the *exact same key
object* to read a value back. Reconstructing a key with an identical label returns `null`
silently — no error. This is why framework keys are declared as shared singletons on
internal `Keys` objects (`modules/core/sources-jvm/Keys.kt` and per-module `Keys.kt`
files).

When adding a new property/extension/component key: declare it once as a `val` and share
it; never build a fresh key at each call site.

Note: DI keys are a *separate* hierarchy and do **not** follow this rule.
`RaptorDIKey` (`modules/di/sources-jvm/di/RaptorDIKey.kt`) is its own interface in the
`io.fluidsonic.raptor.di` package, not a `RaptorKey`, and `KTypeDIKey`
(`modules/di/sources-jvm/di/KTypeDIKey.kt`) overrides `equals`/`hashCode` on the `KType`,
so DI keys are matched by *value* (type), the opposite of identity keys.

Related: `assembly/registries.md` (write-once registration semantics).
