package io.fluidsonic.raptor.bson

import kotlin.reflect.*


internal class DefaultRaptorBsonDefinition<Value : Any>(
	private val additionalDefinitions: List<RaptorBsonDefinition>,
	private val decode: (RaptorBsonReaderScope.(arguments: List<KTypeProjection>?) -> Value)?,
	private val encode: (RaptorBsonWriterScope.(value: Value) -> Unit)?,
	private val encodesSubclasses: Boolean,
	override val valueClass: KClass<Value>,
) : RaptorBsonDefinition.ForValue<Value> {

	private fun <Value : Any> additionalCodeForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry): RaptorBsonCodec<Value>? {
		for (definition in additionalDefinitions)
			definition.codecForValueClass(valueClass, registry = registry)?.let { return it }

		return null
	}


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry) = when {
		valueClass == this.valueClass -> DefaultRaptorBsonCodec(
			decode = decode as (RaptorBsonReaderScope.(arguments: List<KTypeProjection>?) -> Value)?,
			encode = encode as (RaptorBsonWriterScope.(value: Value) -> Unit)?,
			valueClass = valueClass
		)

		encodesSubclasses && this.valueClass.java.isAssignableFrom(valueClass.java) -> DefaultRaptorBsonCodec(
			decode = null,
			encode = encode as (RaptorBsonWriterScope.(value: Value) -> Unit)?,
			valueClass = valueClass
		)

		else -> additionalCodeForValueClass(valueClass, registry = registry)
	}


	override fun toString() = buildString {
		append("Raptor BSON definition (")
		append(valueClass.qualifiedName)
		if (encodesSubclasses)
			append(" + subclasses")
		append(")")
	}
}
