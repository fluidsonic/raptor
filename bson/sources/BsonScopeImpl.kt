package io.fluidsonic.raptor

import org.bson.codecs.configuration.*


internal class BsonScopeImpl(
	config: BsonConfig,
	scope: RaptorScope
) : BsonScope, RaptorScope by scope {

	override val codecRegistry = CodecRegistries.fromCodecs(config.definitions.map { it.codec(this) })!!
}
