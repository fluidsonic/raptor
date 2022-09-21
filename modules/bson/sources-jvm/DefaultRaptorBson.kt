package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import org.bson.codecs.configuration.*


internal class DefaultRaptorBson(
	private val context: RaptorContext,
	override val definitions: List<RaptorBsonDefinition>,
) : RaptorBson {

	override val codecRegistry: CodecRegistry by lazy {
		scope.codecRegistry.internal()
	}


	override val scope: RaptorBsonScope by lazy {
		DefaultRaptorBsonScope(definitions = definitions, context = context)
	}
}
