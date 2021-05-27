package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlin.time.*
import kotlin.time.Duration.*


@OptIn(ExperimentalTime::class)
public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Duration> {
	decode(::parse)
	encode(Duration::toString)
}
