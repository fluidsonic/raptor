package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*


internal class DefaultRaptorBsonScope(
	definitions: List<RaptorBsonDefinition>,
	context: RaptorContext,
) : RaptorBsonScope, RaptorScope by context {

	override val codecRegistry = DefaultBsonRootCodecRegistry(definitions = definitions, scope = this)
}
