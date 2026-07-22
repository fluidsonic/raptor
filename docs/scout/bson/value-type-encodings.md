# BSON wire formats for built-in value types

Arbitrary but fixed on-disk encodings for library value types. Mismatched data throws;
raw Mongo documents look surprising without this.

- **`GeoCoordinate` is a GeoJSON Point with longitude BEFORE latitude.**
  `GeoCoordinate.bsonDefinition()` writes `{"coordinates": [longitude, latitude],
  "type": "Point"}` and decodes in that same order (asserting `type == "Point"`). The
  `GeoCoordinate` constructor takes `(latitude, longitude)` — the opposite order — so
  mixing them silently swaps coordinates. lon-first matches the MongoDB/GeoJSON convention.
  Anchor: `modules/bson/sources-jvm/extensions/GeoCoordinate.kt`.
- **`java.time.DayOfWeek` persists as lowercase English names** `"monday"`…`"sunday"`
  (not ordinal, not `"MONDAY"`, not ISO number); any other string throws. Anchor:
  `modules/bson/sources-jvm/extensions/DayOfWeek.kt` (`DayOfWeek_bsonDefinition`).
- **`LocalDate`/`LocalTime` persist as UTC BSON datetimes** (BSON type `date`, written via
  `writer.value(Instant)` → `writeDateTime`; not the MongoDB internal `timestamp` type). A
  `LocalDate` stores as start-of-day UTC; a `LocalTime` stores as an instant anchored to the
  fixed reference date `1970-01-01` UTC (a private constant), discarding the irrelevant
  component on decode. So in raw data a `LocalTime` looks like a 1970-01-01 datetime. (The
  reader method is named `timestamp()` after fluid-time's `Timestamp`, but it reads a BSON
  datetime — see `DefaultBsonReaderScope.timestamp` calling `readDateTime`.) Anchors:
  `modules/bson/sources-jvm/extensions/LocalDate.kt`,
  `modules/bson/sources-jvm/extensions/LocalTime.kt` (`referenceDate`).
- **`Timestamp` is `kotlin.time.Instant`.** The definition factory is written as an
  extension on `Instant.Companion` yet builds `definition<Timestamp>` — this type-checks
  only because `io.fluidsonic.time.Timestamp` *is* `kotlin.time.Instant`. Anchor:
  `modules/bson/sources-jvm/extensions/Timestamp.kt`.

**Convention:** value-type definition factories are extensions on the type's companion,
called as `Country.bsonDefinition()`, `Currency.bsonDefinition()`, etc. Several files
(e.g. `GeoCoordinate.kt`, `LocalDate.kt`, `TimeZone.kt` under
`modules/bson/sources-jvm/extensions/`) star-import the type's members so the receiver is
written as bare `Companion`. `DayOfWeek` (a java.time type whose companion can't be
extended) uses a free function `DayOfWeek_bsonDefinition()` with `@Suppress("FunctionName")`.

The BSON plugin works without DI: `RaptorBsonPlugin` registers its component
unconditionally but only wires DI providers inside `optional(RaptorDIPlugin) { }`.
</content>
