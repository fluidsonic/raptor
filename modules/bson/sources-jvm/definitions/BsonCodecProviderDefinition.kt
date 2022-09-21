package io.fluidsonic.raptor.bson

import kotlin.reflect.*
import org.bson.codecs.configuration.*


internal class BsonCodecProviderDefinition(
	val codecProvider: CodecProvider,
) : RaptorBsonDefinition {

	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry) =
		codecProvider.get(valueClass.java, registry.internal())
			?.let { BsonCodecDefinition(it) }


	override fun equals(other: Any?) =
		this === other || (other is BsonCodecProviderDefinition && codecProvider == other.codecProvider)


	override fun hashCode() =
		codecProvider.hashCode()


	override fun toString() =
		"Raptor BSON definition ($codecProvider)"
}
