# BSON reader/writer conventions (positional, null-handling, coercions)

The `RaptorBson` reader/writer DSL behaves differently from a map-like document API.
Getting these wrong silently produces incompatible documents or runtime failures.

- **Reading is strictly positional; field names are asserted, not searched.**
  `RaptorBsonReader.fieldName(name)` reads the *next* field name and `check()`s it equals
  the expected name ("Expected field name 'x' but found 'y'"). All by-name helpers
  (`string(field)`, `int(field)`, `document(field){}`, `value(field)`) build on this, so a
  decoder requires fields in the exact order it reads them — there is no random-access
  lookup. To tolerate arbitrary order, use `documentByField {}` and switch on the field name
  yourself (as `extensions/GeoCoordinate.kt` does). Anchor: `RaptorBsonReader.kt` (`fieldName`).
- **`reader.value<T>()` only skips a stored NULL when `T` is nullable.** It checks
  `bsonType() == NULL && typeOf<Value>().isMarkedNullable`; a stored NULL read into a
  non-nullable `T` is *not* skipped and is passed to the codec (typically failing). Unlike
  the `*OrNull` helpers, null-tolerance is gated purely on the call-site's declared
  nullability. Anchor: `RaptorBsonReader.kt` (`value`).
- **`writer.value(field, value, preserveNull = false)` omits null fields by default.** A
  null value with `preserveNull` false writes *nothing* — no field appears in the document.
  Pass `preserveNull = true` to emit an explicit BSON null. `valueAs` follows the same rule.
  Anchor: `RaptorBsonWriter.kt` (`value`, `valueAs`).
- **`timestamp()` maps to BSON DateTime, not the BSON `Timestamp` wire type.** The reader
  calls `readDateTime()`; the writer calls `writeDateTime(...)`. A fluid-time `Timestamp`
  persists as a BSON UTC datetime.
- **Numeric read/write is asymmetric.** Reader `long()` widens a stored INT32 to Long.
  Writers normalize: `value(Float)`→`writeDouble`, `value(Short)`→`writeInt32`; there is no
  `float()`/`short()` reader, so a `Float` round-trips through `Double`.

Generic element types reach a decoder only via `DefaultScopedBsonCodec` — see
`bson/codec-resolution.md`. Collection decoders (`List.kt`, `Collection.kt`) read
`arguments?.singleOrNull()?.type`, falling back to a runtime-BSON-type map when raw.
</content>
