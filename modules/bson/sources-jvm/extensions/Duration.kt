package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import kotlin.time.*
import kotlin.time.Duration.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Duration> {
	decode<String>(::parseIsoString)
	encode(Duration::toIsoString)
}
