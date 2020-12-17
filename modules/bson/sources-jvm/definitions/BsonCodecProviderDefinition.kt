package io.fluidsonic.raptor.bson.internal

import io.fluidsonic.raptor.*
import kotlin.reflect.*
import org.bson.codecs.configuration.*


internal class BsonCodecProviderDefinition(
	val codecProvider: CodecProvider,
) : RaptorBsonDefinition {

	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry) =
		codecProvider.get(valueClass.java, registry.internal())
			?.let { BsonCodecDefinition(it) }
}
