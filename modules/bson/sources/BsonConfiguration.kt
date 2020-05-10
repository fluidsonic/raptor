package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*


// FIXME find a different way that maintains order
class BsonConfiguration(
	val codecs: Collection<Codec<*>>,
	val definitions: Collection<RaptorBsonDefinition<*>>,
	val providers: Collection<CodecProvider>,
	val registries: Collection<CodecRegistry>
) {

	companion object {

		val empty = BsonConfiguration(
			codecs = emptyList(),
			definitions = emptyList(),
			providers = emptyList(),
			registries = emptyList()
		)
	}


	internal object PropertyKey : RaptorPropertyKey<BsonConfiguration> {

		override fun toString() = "bson"
	}
}
