package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun Timestamp.Companion.bsonDefinition() = bsonDefinition(
	parse = Timestamp::parse,
	serialize = Timestamp::toString
)

