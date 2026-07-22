# Scalar mapping quirks and the `Any`-as-generic-sentinel

Individual scalar-definition surprises plus the hand-rolled generic type machinery.

Scalar mapping:
- **Kotlin `Double` maps to a GraphQL scalar named `"Float"`** (explicit `name = "Float"`
  override in `Double.Companion.graphDefinition()`); Int/String/Boolean keep type-derived
  names.
- **Kotlin `Set` maps to a GraphQL list** (`Set::class` handled alongside `Collection`/`List`
  in `typeRef`), losing uniqueness semantics.
- **`Unit.graphDefinition()` is a non-functional stub:** `parseInt { TODO() }` (throws
  `NotImplementedError` if used as input) and `serialize { 42 }` (always outputs literal 42);
  the intended `outputOnly()` is commented out. Using `Unit` as a GraphQL input crashes.
- **The `Timestamp` scalar is defined on `Instant.Companion` but typed/named `Timestamp`**
  (io.fluidsonic.time), serializing via `Timestamp::toString`. In the `graphql` module the
  factory is misspelled **plural** `Instant.Companion.graphDefinitions()` — callers must use
  the plural name there.
- **`GraphId` (name `"ID"`) coerces both Int and String inputs** (Int via `toString()`),
  else `invalid()`. Typed `parse*` helpers wrap generic parse with `input as? T ?: invalid()`;
  string-based domain scalars parse via `<helper>(it) ?: invalid()` (CountryCode's
  `parseOrNull`, Currency's `forCodeOrNull`, Duration's `parseIsoStringOrNull`). `invalid()`
  is the sanctioned rejection path.
- `KType.defaultGraphName()` (graph module `extensions/KType.kt`) derives the default name
  from `classifier.simpleName` and hard-errors for any non-`KClass` classifier (e.g. a type
  parameter). An explicit `name =` overrides that default — which is why `Float` and `ID`
  differ from their Kotlin class names (`Double`, `GraphId`).
- `LocalTime.kt` uses fully-qualified `kotlinx.datetime.LocalTime` deliberately (comment:
  IDEA's Optimize Imports strips the `kotlinx.datetime.*` import) — do not shorten it.

Generics (`KotlinType.kt`, a hand-rolled reflection wrapper, *not* `kotlin.reflect.KType`):
- **`Any::class` is a sentinel for "totally generic".** `KotlinType.of(...)` never returns
  null — an unmappable/erased type parameter falls back to `KotlinType(classifier =
  Any::class)` (comment "so many hacks… basically means totally generic type"). `isGeneric`
  is true for `Any::class`; `specialize()` special-cases it. Generic types are lazily
  specialized on demand, so `resolveAllReferences` is genuinely recursive (declared `tailrec`)
  because resolving a reference can register new definitions. A generic type whose parameter
  upper bound resolves to `Any` is **silently dropped** from the final schema (no diagnostic).
</content>
