package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*


class BsonConfig(
	val codecs: Collection<Codec<*>>,
	val definitions: Collection<RaptorBsonDefinition<*>>,
	val providers: Collection<CodecProvider>,
	val registries: Collection<CodecRegistry>
) {

	companion object {

		val empty = BsonConfig(
			codecs = emptyList(),
			definitions = emptyList(),
			providers = emptyList(),
			registries = emptyList()
		)
	}
}
