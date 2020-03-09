package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun PreciseDuration.Companion.bsonDefinition() = bsonDefinition(
	parse = PreciseDuration::parse,
	serialize = PreciseDuration::toString
)


fun PreciseDuration.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::parse)

		parseJson(::parse)
		serializeJson(PreciseDuration::toString)
	}
}
