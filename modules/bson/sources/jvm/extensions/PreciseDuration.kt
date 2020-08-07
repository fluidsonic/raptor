package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun PreciseDuration.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = PreciseDuration::parse,
	serialize = PreciseDuration::toString
)
