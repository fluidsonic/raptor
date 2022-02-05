package io.fluidsonic.raptor

import kotlin.time.*
import kotlin.time.Duration.*


@OptIn(ExperimentalTime::class)
public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Duration> {
	decode<String>(::parseIsoString)
	encode(Duration::toIsoString)
}
