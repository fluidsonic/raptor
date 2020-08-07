package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun TimeZone.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = TimeZone::withId,
	serialize = TimeZone::toString
)
