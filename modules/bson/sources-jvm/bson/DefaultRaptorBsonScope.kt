package io.fluidsonic.raptor.bson.internal

import io.fluidsonic.raptor.*


internal class DefaultRaptorBsonScope(
	definitions: List<RaptorBsonDefinition>,
	context: RaptorContext,
) : RaptorBsonScope, RaptorScope by context {

	override val codecRegistry = DefaultBsonRootCodecRegistry(definitions = definitions, scope = this)
}
