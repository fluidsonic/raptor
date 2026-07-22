# MongoDB module: fluidsonic wrapper and DSL footguns

The `mongodb` module wraps a specific driver and its DSL has several silent traps.

version: io.fluidsonic.mongo 1.9.0 (as of 2026-07-22; declared as `Versions.fluid_mongo` in `buildSrc/sources/Versions.kt`)

- **It uses the `io.fluidsonic.mongo` wrapper, not the raw MongoDB driver.** All
  collection/database/flow types are `io.fluidsonic.mongo.MongoCollection` / `MongoDatabase` /
  `FindFlow` — a coroutine-first wrapper over the reactive driver: write methods are `suspend`,
  reads return kotlinx `Flow`/`FindFlow`, and result types are chosen with Kotlin
  `KClass<out TResult>` rather than `java.lang.Class`. Not `com.mongodb.reactivestreams` or the
  sync driver.
- **`updateOne` with no change operations silently degrades to a read.**
  `RaptorMongoUpdate.execute` treats a null `changes` (no `set`/`unset`/`addToSet` produced) as
  a lookup: it calls `find(filter).firstOrNull()` and never writes. So `collection.updateOne {
  filter { ... } }` with only a filter returns the matching document unchanged.
  **`upsertOne` still force-unwraps with `!!`**, so an upsert that produced no changes NPEs on
  an empty collection. Anchor: `dsl/RaptorMongoUpdate.kt` (`execute`, `upsertOne`).
- **Default codec registry omits enum and Kotlin codecs.** `includeMongoClientDefaultCodecs`
  registers a curated provider list but comments out `EnumCodecProvider()` and
  `KotlinCodecProvider()` ("Must be encoded explicitly.") — enums and Kotlin data classes do
  NOT auto-encode and require explicit BSON codec registration. The registry is added at
  `RaptorBsonDefinition.Priority.low` and installation is idempotent. Anchor:
  `modules/mongodb/sources-jvm/extensions/BsonRaptorComponent.kt`
  (`includeMongoClientDefaultCodecs`, `defaultMongoCodecRegistry`).
- **`MongoClient.transaction` does not scope operations or retry.** It runs `block(session)` in
  try/catch (aborting on throw) but calls `commitTransaction()` *after* the try block, passes
  the `ClientSession` to the lambda but wires it into no operation (callers must thread it
  manually), and has no retry loop for `TransientTransactionError`/`UnknownTransactionCommitResult`.
- **Generic-typed collections are decode-only.** `MongoDatabase.getCollectionOfGeneric` wraps
  the codec in a private `GenericBsonCodec` whose `encode` throws unconditionally — any
  insert/replace fails at runtime. It also requires the codec to implement `CodecEx`.
- `findById(ids)` short-circuits to `FindFlow.empty()` for an empty id set (avoiding `$in []`).
  Single-doc by-id helpers mix `Filters.eq(id)` (targets `_id`) and explicit `eq("_id", id)` —
  both valid driver idioms.
- **Update builder uses `Maybe<*>` for partial updates:** absent `Maybe` = leave field
  untouched, present null with `setOrUnsetIfNull` = `$unset`, present value = `$set`.
</content>
