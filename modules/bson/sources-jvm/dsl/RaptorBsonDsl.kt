@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import kotlin.internal.*
import kotlin.reflect.*
import kotlinx.serialization.modules.*


public object RaptorBsonDsl


@RaptorDsl
@Suppress("UnusedReceiverParameter")
public val RaptorGlobalDsl.bson: RaptorBsonDsl
	get() = RaptorBsonDsl


/**
 * Creates a definition that reads and writes [Value] field by field, using the `decode { … }` and
 * `encode { … }` blocks the [configure] builder collects.
 */
// https://youtrack.jetbrains.com/issue/KT-54477/NoInfer-doesnt-work-for-builders
@RaptorDsl
public inline fun <reified Value : Any> RaptorBsonDsl.definition(
	noinline configure: RaptorBsonDefinitionBuilder<@NoInfer Value>.() -> Unit,
): RaptorBsonDefinition.ForValue<@NoInfer Value> =
	definition(valueClass = Value::class, configure = configure)


/**
 * Creates a definition for [valueClass], for the reflective case where [Value] is not statically known.
 *
 * @throws IllegalStateException if [configure] provides neither a `decode { … }` nor an `encode { … }` block.
 */
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun <Value : Any> RaptorBsonDsl.definition(
	valueClass: KClass<Value>,
	configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit,
): RaptorBsonDefinition.ForValue<Value> =
	RaptorBsonDefinitionBuilder(valueClass = valueClass).apply(configure).build()


/**
 * Creates a reusable [RaptorBsonType] for [Value], to be resolved once and then reused across reads and
 * writes of every field of that type.
 *
 * See [RaptorBsonType] for what the resolve-once token saves over `reader.value<Value>(field)`.
 */
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public inline fun <reified Value : Any> RaptorBsonDsl.type(): RaptorBsonType<Value> =
	type(typeOf<Value>())


/**
 * Creates a reusable [RaptorBsonType] for [type], for the reflective case where [Value] is not statically
 * known.
 *
 * @throws IllegalArgumentException if [type] is marked nullable — nullability is a call-site choice, never a
 *   property of the type token itself.
 */
@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun <Value : Any> RaptorBsonDsl.type(type: KType): RaptorBsonType<Value> {
	require(!type.isMarkedNullable) { "RaptorBsonType can only be created for a non-null type, but got '$type'." }

	return RaptorBsonType(type)
}


/**
 * Creates a definition that reads and writes [Value] through the serializer `kotlinx.serialization` resolves
 * for it, deriving the BSON field names and types from [Value]'s `@Serializable` declaration.
 *
 * Internal and experimental — not public API in this release; see [RaptorBsonSerializationCodec] for why.
 *
 * This is the counterpart to [definition], which spells the same wire format out by hand instead. Both
 * produce byte-identical BSON for the identical record shape, so a type can be migrated from one to the other
 * without rewriting stored documents. See [RaptorBsonSerializationCodec] for the supported property types and
 * for the escape hatch a custom `KSerializer` uses to reach the raw `BsonReader`/`BsonWriter`.
 *
 * @param serializersModule The module used to resolve contextual and polymorphic serializers. Pass
 *   `EmptySerializersModule()` when [Value] needs neither.
 * @param preserveNulls Whether a null property is written as an explicit BSON null (`true`) or the field is
 *   omitted from the document entirely (`false`, matching `writer.value(field, value)` in a hand-written
 *   [definition]). Omitting requires nullable properties to declare a Kotlin default — see
 *   [RaptorBsonSerializationCodec].
 * @throws kotlinx.serialization.SerializationException if no serializer can be resolved for [Value].
 */
@RaptorDsl
@Suppress("UnusedReceiverParameter")
internal inline fun <reified Value : Any> RaptorBsonDsl.serializableDefinition(
	serializersModule: SerializersModule,
	preserveNulls: Boolean = false,
): RaptorBsonDefinition =
	RaptorBsonDefinition.of(
		RaptorBsonSerializationCodec.of<Value>(serializersModule = serializersModule, preserveNulls = preserveNulls),
	)
