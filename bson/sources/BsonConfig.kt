package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*


internal class BsonConfig(
	val codecs: Collection<Codec<*>>,
	val definitions: Collection<RaptorBsonDefinition<*>>,
	val providers: Collection<CodecProvider>,
	val registries: Collection<CodecRegistry>
)
