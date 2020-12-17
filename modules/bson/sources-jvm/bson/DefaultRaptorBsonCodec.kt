package io.fluidsonic.raptor.bson.internal

import io.fluidsonic.raptor.*
import kotlin.reflect.*


internal class DefaultRaptorBsonCodec<Value : Any>(
	private val decode: (RaptorBsonReaderScope.(arguments: List<KTypeProjection>?) -> Value)?,
	private val encode: (RaptorBsonWriterScope.(value: Value) -> Unit)?,
	override val valueClass: KClass<Value>,
) : RaptorBsonCodec<Value> {

	override fun RaptorBsonReaderScope.decode(arguments: List<KTypeProjection>?): Value =
		decode?.invoke(this, arguments) ?: error(
			"No BSON decoder was provided for type '${valueClass.qualifiedName}'.\n" +
				"Add a `decode { … }` block to your `raptor.bson.definition { … }` for that type."
		)


	override fun RaptorBsonWriterScope.encode(value: Value) {
		encode?.invoke(this, value) ?: error(
			"No BSON encoder was provided for type '${valueClass.qualifiedName}'.\n" +
				"Add an `encode { … }` block to your `raptor.bson.definition { … }` for that type."
		)
	}


	override fun toString() =
		"Raptor BSON codec (${valueClass.qualifiedName})"
}
