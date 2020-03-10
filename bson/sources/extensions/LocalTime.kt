package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalTime.Companion.bsonDefinition() = bsonDefinition(
	parse = LocalTime::parse,
	serialize = LocalTime::toString
)
