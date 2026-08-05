# Root serializer resolution must go through the caller's SerializersModule

A kotlinx.serialization footgun in `RaptorBsonSerializationCodec.of()` — two same-shaped
calls that typecheck identically but behave differently.

`of()` (`modules/bson/sources-jvm/serialization/RaptorBsonSerializationCodec.kt`) resolves
the root `KSerializer<Value>` via `serializersModule.serializer<Value>()` — the reified
extension `SerializersModule.serializer` (`kotlinx.serialization.Serializers.kt`, package
`kotlinx.serialization`, version 1.10.0 as of this pass) — not the format-agnostic
top-level `kotlinx.serialization.serializer<Value>()` function. Both are reified,
zero-argument-shaped calls on a type parameter and compile cleanly in either spot, so
swapping back to the bare top-level function is an easy, silent regression: it ignores the
caller-supplied `SerializersModule` entirely, so a root `Value` covered only via
`SerializersModule.contextual` (never annotated `@Serializable`) throws
`SerializationException` at the first `of()` call instead of at compile time.

Nested properties don't have this trap — `decode`/`encode` already thread
`serializersModule` into `DefaultBsonSerializationDecoder`/`DefaultBsonSerializationEncoder`,
which kotlinx.serialization's generated code consults automatically. Only the *root*
lookup inside `of()` needed the explicit module-qualified call.

Regression test: `ofResolvesARootSerializerThroughTheSerializersModule` in
`modules/bson/tests-jvm/SerializationCodecBsonTests.kt`, using the `ContextualRootProbe`
fixture in `modules/bson/tests-jvm/SerializationCodecFixtures.kt`.
