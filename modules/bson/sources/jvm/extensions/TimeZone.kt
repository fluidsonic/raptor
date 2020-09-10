package io.fluidsonic.raptor

import io.fluidsonic.time.*
import io.fluidsonic.time.TimeZone.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<TimeZone> {
	decode<String> { withId(it) ?: error("Unknown TimeZone ID: $it") } // FIXME move non-null parsing to fluid-time
	encode(TimeZone::toString)
}
