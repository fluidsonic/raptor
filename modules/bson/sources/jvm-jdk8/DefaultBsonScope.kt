package io.fluidsonic.raptor

import org.bson.codecs.configuration.*


internal class DefaultBsonScope(
	configuration: BsonConfiguration,
	context: RaptorContext
) : BsonScope, RaptorScope by context {

	// FIXME This doesn't maintain order between definitions, codecs & registries. How to handle overrides?
	override val codecRegistry = CodecRegistries.fromRegistries(
		*configuration.registries.toTypedArray(),
		CodecRegistries.fromProviders(
			*configuration.providers.toTypedArray(),
			*configuration.definitions.mapNotNull { it.provider(this) }.toTypedArray()
		),
		CodecRegistries.fromCodecs(
			*configuration.codecs.toTypedArray(),
			*configuration.definitions.mapNotNull { it.codec(this) }.toTypedArray()
		),
		*configuration.definitions.mapNotNull { it.registry(this) }.toTypedArray()
	)!!
}
