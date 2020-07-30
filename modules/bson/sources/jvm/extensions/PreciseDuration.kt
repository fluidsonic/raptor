package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun PreciseDuration.Companion.bsonDefinition() = bsonDefinition(
	parse = PreciseDuration::parse,
	serialize = PreciseDuration::toString
)
