@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import kotlin.internal.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorBsonDefinitionBuilder<Value : Any> internal constructor(
	internal val valueClass: KClass<Value>,
) {

	private var additionalDefinitions: List<RaptorBsonDefinition> = emptyList()
	private var decode: (RaptorBsonReaderScope.(arguments: List<KTypeProjection>?) -> Value)? = null
	private var encode: (RaptorBsonWriterScope.(value: Value) -> Unit)? = null
	private var encodesSubclasses = false


	internal fun build(): RaptorBsonDefinition.ForValue<Value> {
		check(decode != null || encode != null) { "A `decode { … }` block, an `encode { … }` block, or both must be provided." }

		return DefaultRaptorBsonDefinition(
			additionalDefinitions = additionalDefinitions,
			decode = decode,
			encode = encode,
			encodesSubclasses = encodesSubclasses,
			valueClass = valueClass,
		)
	}


	@RaptorDsl
	public fun additionalDefinitions(vararg definitions: RaptorBsonDefinition) {
		additionalDefinitions(definitions.toList())
	}


	@RaptorDsl
	public fun additionalDefinitions(definitions: Iterable<RaptorBsonDefinition>) {
		additionalDefinitions = additionalDefinitions + definitions
	}


	// FIXME Enable BuilderInference once fixed: https://youtrack.jetbrains.com/issue/KT-41595
	@RaptorDsl
	public inline fun <reified Value : Any> additionalDefinition(
		/* @BuilderInference */
		noinline configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit,
	) {
		additionalDefinition(valueClass = Value::class, configure = configure)
	}


	@RaptorDsl
	public fun <Value : Any> additionalDefinition(valueClass: KClass<Value>, configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit) {
		additionalDefinitions = additionalDefinitions + raptor.bson.definition(valueClass = valueClass, configure = configure)
	}


	@RaptorDsl
	public fun decode(decode: RaptorBsonReaderScope.(arguments: List<KTypeProjection>?) -> Value) {
		check(this.decode == null) { "Cannot provide multiple `decode { … }` blocks." }

		this.decode = decode
	}


	// TODO add more primitive overloads to avoid codecs
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public inline fun <reified DecodedValue : Any> decode(noinline decode: (value: DecodedValue) -> Value) {
		decode {
			decode(reader.value())
		}
	}


	@JvmName("decodeInt")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun decode(decode: (value: Int) -> Value) {
		decode {
			decode(reader.int())
		}
	}


	@JvmName("decodeLong")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun decode(decode: (value: Long) -> Value) {
		decode {
			decode(reader.long())
		}
	}


	@JvmName("decodeString")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun decode(decode: (value: String) -> Value) {
		decode {
			decode(reader.string())
		}
	}


	@RaptorDsl
	public fun encode(includingSubclasses: Boolean = false, encode: RaptorBsonWriterScope.(value: Value) -> Unit) {
		check(this.encode == null) { "Cannot provide multiple `encode { … }` blocks." }

		this.encode = encode
		this.encodesSubclasses = includingSubclasses
	}


	// TODO add more primitive overloads to avoid codecs
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun <EncodedValue : Any> encode(encode: (value: Value) -> EncodedValue) {
		encode { value ->
			writer.value(encode(value))
		}
	}


	@JvmName("encodeInt")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun encode(encode: (value: Value) -> Int, includingSubclasses: Boolean = false) {
		encode(includingSubclasses = includingSubclasses) { value ->
			writer.value(encode(value))
		}
	}


	@JvmName("encodeLong")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun encode(encode: (value: Value) -> Long, includingSubclasses: Boolean = false) {
		encode(includingSubclasses = includingSubclasses) { value ->
			writer.value(encode(value))
		}
	}


	@JvmName("encodeString")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun encode(encode: (value: Value) -> String, includingSubclasses: Boolean = false) {
		encode(includingSubclasses = includingSubclasses) { value ->
			writer.value(encode(value))
		}
	}
}
