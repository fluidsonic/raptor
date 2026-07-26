# BSON codec resolution, ordering, and MongoDB bridging

How the BSON module assembles and resolves codecs — a set of ordering/precedence rules
with a documented crash consequence.

- **Definition merge order is load-bearing.** `RaptorBsonComponent.onConfigurationEnded`
  flattens definitions in a fixed order: `Priority.high`, `Priority.normal`,
  `raptorDefaults`, `Priority.low`, then `bsonDefaults` **last** (after even
  `Priority.low`). A code comment states these MongoDB defaults must come last so other
  codecs can override default behavior, *and* that "MongoDB would freak out with
  StackOverflowError if their own codecs don't come before these!" `includeDefaultDefinitions()`
  always moves the default set to the very end regardless of where it is called in the DSL.
- **Precedence is first-match-wins over ordered lists.** `RaptorBsonDefinition.Companion`
  exposes `bsonDefaults` (four org.bson providers in fixed order) and `raptorDefaults`
  (project value types, with `List` listed before `Collection` even though List is a
  Collection). `codecs()` and `providers()` wrap their inputs via `RaptorBsonDefinition.of(...)`
  before funneling into `definitions()`, which appends everything to `definitionsByPriority`
  in DSL call order; within a priority bucket they merge as peers.
- **MongoDB bridging.** `DefaultBsonRootCodecRegistry` *is* a `CodecProvider` and registers
  itself via `CodecRegistries.fromProviders(this)`. A resolved codec is returned directly
  only when it is a `Codec` whose `encoderClass == valueClass`; otherwise it is wrapped in
  `DefaultScopedBsonCodec`. That exact-encoderClass gate distinguishes pass-through MongoDB
  codecs from Raptor codecs needing scope. Raw org.bson codecs match by **exact class**
  (`encoderClass == valueClass.java`), while `DefaultRaptorBsonDefinition`'s
  `encodesSubclasses` path uses `isAssignableFrom`.
- **Negative lookups are cached via a `NullCodec` sentinel.** Two `ConcurrentHashMap` caches
  can't hold nulls, so misses store a private `NullCodec` (every method throws
  "Not possible.") filtered out with `takeIf { it !== NullCodec }`.
- `encode(includingSubclasses = true)` produces subclass codecs with `decode = null` —
  polymorphic for writing, but subclasses have **no decoder**.

Anchors: `RaptorBsonComponent.kt`, `DefaultBsonRootCodecRegistry.kt`,
`DefaultScopedBsonCodec.kt`, `definitions/DefaultRaptorBsonDefinition.kt`,
`definitions/BsonCodecDefinition.kt`.
