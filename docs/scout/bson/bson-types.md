# RaptorBsonType: resolve-once dispatch, and its read/write asymmetry

`raptor.bson.type<T>()` (`modules/bson/sources-jvm/dsl/RaptorBsonDsl.kt`, the `type`
factory) builds a reusable `RaptorBsonType<T>` (`modules/bson/sources-jvm/bson/RaptorBsonType.kt`)
that a decoder/encoder resolves once (typically into a `val` outside the `decode`/`encode`
block) and then reads/writes via `RaptorBsonReader.valueOrThrow(type)` /
`RaptorBsonReader.valueOrNull(type)` / `RaptorBsonWriter.value(type, value)`, instead of
resolving type dispatch and the codec registry on every call the way the bare
`reader.value<T>(field)` / `writer.value(field, value)` do.

**The performance payoff is not symmetric between read and write, and it takes comparing
both interfaces side by side to see why — not just one file.** On decode,
`RaptorBsonReader`'s bare path (`value<Value>()`/`value(field)`) is a single reified
generic function with no per-primitive overload, so it boxes; the primitive-specialized
`valueOrThrow(type: RaptorBsonType<Boolean/Double/Int/Long>)` overloads
(`DefaultBsonReaderScope`) route straight to `boolean()`/`double()`/`int()`/`long()` instead
and skip that box. On encode there is no equivalent box to remove, because `RaptorBsonWriter`
already ships bare per-primitive overloads (`value(field: String, value: Boolean)`, etc.) —
so the primitive-specialized `RaptorBsonType`-based write overloads (`DefaultBsonWriterScope`,
same signatures) just forward to the pre-existing unboxed `value(Boolean)`/`value(Double)`/
`value(Int)`/`value(Long)` methods, gaining nothing the bare write path didn't already have.
The only measurable write-side win is on codec-backed (non-primitive) fields: the cached
`Binding` (`RaptorBsonType.codec`) skips the `value::class` reflection and the
`RaptorBsonCodecRegistry` hashmap lookup that the generic `valueAs` path
(`DefaultBsonWriterScope.valueAs`) repeats on every call.

Related: reader-writer-conventions.md, codec-resolution.md.
