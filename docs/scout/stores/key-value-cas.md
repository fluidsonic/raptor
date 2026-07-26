# RaptorKeyValueStore.update — optimistic compare-and-swap contract

`update()` promises an atomic read-modify-write, but the backends implement it very
differently, and the callback carries an unusual constraint.

- **The `decide` callback is non-suspending and must be pure.** Signature is
  `(current: Value?) -> UpdateDecision<Value>` — a plain, non-`suspend` function. The KDoc
  requires it to be a pure function of its argument with no side effects and no store access,
  because the optimistic backend may invoke it multiple times on conflict (per its KDoc). Note
  it is a plain lambda, not `suspend`, so it cannot itself do suspending work.
  Anchor: `modules/store/sources-jvm/RaptorKeyValueStore.kt` (`update`).
- **Mongo backend does whole-document CAS.** `MongoKeyValueStore.update` opens a
  `RawBsonDocument` view, reads the entire current document, then uses those exact stored bytes
  as the `replaceOne`/`deleteOne` **filter** — the write applies only if the document is
  byte-for-byte unchanged since the read (else `matchedCount == 0` → re-read + re-decide, up to
  `maxAttempts`, then `RaptorOptimisticUpdateException`). New-key insertion uses `insertIfAbsent`,
  treating a Mongo write error whose `ErrorCategory` is `DUPLICATE_KEY` (Mongo code 11000) as a
  lost race → retry; any other write error rethrows. Anchor:
  `modules/store-mongo/sources-jvm/MongoKeyValueStore.kt` (`update`, `insertIfAbsent`).
- **Memory backend never retries and ignores `maxAttempts`.** `MemoryKeyValueStore.update`
  delegates to `ConcurrentHashMap.compute`, which is atomic under the bin lock, so `decide`
  runs exactly once and the return value is precisely the post-update value. A test locks in
  the single-invocation guarantee. So `update`'s advertised retry shape is a no-op here.
- **Mongo schema:** `MongoKeyValueStore.Fields` maps `key = "_id"`, `value = "value"`; entries
  are `{ _id: <key>, value: <value> }`, leveraging Mongo's implicit unique `_id` index.
  `setIfAbsent` is an upserting `updateOne` with `$setOnInsert`, detecting insertion via
  `upsertedId != null`. `EntryCodec.decode` reads fields **positionally** (`key` then `value`)
  — documents serialized in a different field order break decoding.

`RaptorLogStore` is write-only by contract — the interface exposes only `append`.
`MemoryLogStore.values()` (a non-override snapshot) is a test/inspection affordance;
`MongoLogStore` provides no read path at all.
