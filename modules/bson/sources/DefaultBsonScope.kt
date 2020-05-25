package io.fluidsonic.raptor

import org.bson.codecs.configuration.*


internal class DefaultBsonScope(
	configuration: BsonConfiguration,
	context: RaptorContext
) : BsonScope, RaptorScope by context {

	// FIXME This doesn't maintain order between definitions, codecs & registries. How to handle overrides?
	override val codecRegistry = CodecRegistries.fromRegistries(
		*configuration.registries.toTypedArray(),
		CodecRegistries.fromProviders(*configuration.providers.toTypedArray()),
		CodecRegistries.fromCodecs(*configuration.codecs.toTypedArray()),
		CodecRegistries.fromCodecs(configuration.definitions.map { it.codec(this) })
	)!!
}
