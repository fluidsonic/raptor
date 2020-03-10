package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun TimeZone.Companion.bsonDefinition() = bsonDefinition(
	parse = TimeZone::withId,
	serialize = TimeZone::toString
)
