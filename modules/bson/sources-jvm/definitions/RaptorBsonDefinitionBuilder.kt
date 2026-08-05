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
	private var decodeWithType: (RaptorBsonReaderScope.(type: RaptorBsonType<*>) -> Value)? = null
	private var encode: (RaptorBsonWriterScope.(value: Value) -> Unit)? = null
	private var encodesSubclasses = false


	internal fun build(): RaptorBsonDefinition.ForValue<Value> {
		check(decode != null || encode != null) { "A `decode { … }` block, an `encode { … }` block, or both must be provided." }
		check(decodeWithType == null || decode != null) {
			"A `decodeWithType { … }` block also requires a `decode { … }` block, which serves the reflective " +
				"`reader.value<…>()` path where no RaptorBsonType is available."
		}

		return DefaultRaptorBsonDefinition(
			additionalDefinitions = additionalDefinitions,
			decode = decode,
			decodeWithType = decodeWithType,
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


	@RaptorDsl
	public inline fun <reified Value : Any> additionalDefinition(
		noinline configure: RaptorBsonDefinitionBuilder<@NoInfer Value>.() -> Unit,
	) {
		additionalDefinition(valueClass = Value::class, configure = configure)
	}


	@RaptorDsl
	public fun <Value : Any> additionalDefinition(valueClass: KClass<Value>, configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit) {
		additionalDefinitions = additionalDefinitions + raptor.bson.definition(valueClass = valueClass, configure = configure)
	}


	@RaptorDsl
	public fun decode(decode: RaptorBsonReaderScope.(arguments: List<KTypeProjection>?) -> @NoInfer Value) {
		check(this.decode == null) { "Cannot provide multiple `decode { … }` blocks." }

		this.decode = decode
	}


	/**
	 * Decodes using the [RaptorBsonType] the value is being read through, whose
	 * [argumentTypes][RaptorBsonType.argumentTypes] and
	 * [argumentNullability][RaptorBsonType.argumentNullability] resolve this type's own generic arguments once
	 * instead of once per read — for a generic type like `Foo<T>` or `Bar<K, V>`.
	 *
	 * Used in place of [decode] whenever the value is read through a [RaptorBsonType], i.e. via
	 * [RaptorBsonReader.valueOrThrow] or [RaptorBsonReader.valueOrNull]. A `decode { … }` block is still
	 * required alongside it, for the reflective `reader.value<…>()` path where no [RaptorBsonType] exists.
	 */
	@RaptorDsl
	public fun decodeWithType(decode: RaptorBsonReaderScope.(type: RaptorBsonType<*>) -> @NoInfer Value) {
		check(this.decodeWithType == null) { "Cannot provide multiple `decodeWithType { … }` blocks." }

		this.decodeWithType = decode
	}


	@LowPriorityInOverloadResolution
	@RaptorDsl
	public inline fun <reified DecodedValue : Any> decode(noinline decode: (value: DecodedValue) -> Value) {
		decode {
			decode(reader.value())
		}
	}


	@JvmName("decodeBoolean")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun decode(decode: (value: Boolean) -> Value) {
		decode {
			decode(reader.boolean())
		}
	}


	@JvmName("decodeDouble")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun decode(decode: (value: Double) -> Value) {
		decode {
			decode(reader.double())
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


	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun <EncodedValue : Any> encode(encode: (value: Value) -> EncodedValue) {
		encode { value ->
			writer.value(encode(value))
		}
	}


	@JvmName("encodeBoolean")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun encode(encode: (value: Value) -> Boolean, includingSubclasses: Boolean = false) {
		encode(includingSubclasses = includingSubclasses) { value ->
			writer.value(encode(value))
		}
	}


	@JvmName("encodeDouble")
	@LowPriorityInOverloadResolution
	@RaptorDsl
	public fun encode(encode: (value: Value) -> Double, includingSubclasses: Boolean = false) {
		encode(includingSubclasses = includingSubclasses) { value ->
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
