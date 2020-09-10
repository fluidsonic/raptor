package io.fluidsonic.raptor.bson.internal

import io.fluidsonic.raptor.*
import kotlin.reflect.*


internal class DefaultRaptorBsonDefinition<Value : Any>(
	private val additionalDefinitions: List<RaptorBsonDefinition>,
	private val decode: (RaptorBsonReaderScope.() -> Value)?,
	private val encode: (RaptorBsonWriterScope.(value: Value) -> Unit)?,
	private val encodesSubclasses: Boolean,
	private val valueClass: KClass<Value>,
) : RaptorBsonDefinition {

	private fun <Value : Any> additionalCodeForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry): RaptorBsonCodec<Value>? {
		for (definition in additionalDefinitions)
			definition.codecForValueClass(valueClass, registry = registry)?.let { return it }

		return null
	}


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry) = when {
		valueClass == this.valueClass -> DefaultRaptorBsonCodec(
			decode = decode as (RaptorBsonReaderScope.() -> Value)?,
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
