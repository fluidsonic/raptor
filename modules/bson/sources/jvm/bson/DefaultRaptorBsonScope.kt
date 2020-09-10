package io.fluidsonic.raptor.bson.internal

import io.fluidsonic.raptor.*


internal class DefaultRaptorBsonScope(
	configuration: RaptorBsonConfiguration,
	context: RaptorContext,
) : RaptorBsonScope, RaptorScope by context {

	override val codecRegistry = DefaultBsonRootCodecRegistry(definitions = configuration.definitions, scope = this)
}
