package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<TimeZone> {
	decode<String> { ofOrNull(it) ?: error("Invalid TimeZone ID: $it") }
	encode(TimeZone::id)
}
