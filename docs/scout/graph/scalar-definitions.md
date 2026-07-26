# Scalar definition quirks

Individual surprises in the shipped scalar definitions
(`modules/graph/sources-jvm/extensions/`, registered in bulk through
`RaptorGraphDefaults.definitions` — see `schema-generation.md`).

- **`Unit.graphDefinition()` is a non-functional stub:** `parseInt { TODO() }` (throws
  `NotImplementedError` if used as input) and `serialize { 42 }` (always outputs literal 42);
  the intended `outputOnly()` is commented out. Using `Unit` as a GraphQL input crashes.
- **The `Timestamp` scalar is declared on `Instant.Companion` but typed and named
  `Timestamp`** (`extensions/Timestamp.kt`). `io.fluidsonic.time.Timestamp` is a typealias for
  `kotlin.time.Instant`, so the call site in `RaptorGraphDefaults` reads
  `Timestamp.graphDefinition()`; serialization goes through `Timestamp::toString`.
- **Rejecting input always goes through `invalid()`.** The typed `parse*` helpers in
  `modules/graph/sources-jvm/definitions/RaptorScalarGraphDefinitionBuilder.kt` wrap the
  generic `parse` with `input as? T ?: invalid()`; the string-based domain scalars parse via
  `<helper>(it) ?: invalid()` (CountryCode's `parseOrNull`, Currency's `forCodeOrNull`,
  Duration's `parseIsoStringOrNull`). See `coercion-scopes.md` for what `invalid()` does.
- `LocalTime.kt` uses fully-qualified `kotlinx.datetime.LocalTime` deliberately (comment:
  IDEA's Optimize Imports strips the `kotlinx.datetime.*` import) — do not shorten it.

Related: `builtin-scalars.md` (`Boolean`, `Float`, `ID`, `Int` and `String` work differently),
`kotlin-type-mapping.md`.
