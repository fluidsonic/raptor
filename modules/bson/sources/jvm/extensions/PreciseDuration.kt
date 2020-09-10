package io.fluidsonic.raptor

import io.fluidsonic.time.*
import io.fluidsonic.time.PreciseDuration.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<PreciseDuration> {
	decode<String> { parse(it) ?: error("Invalid ISO 8601 duration format: $it") } // FIXME move non-null parsing to fluid-time
	encode(PreciseDuration::toString)
}
