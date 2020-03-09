package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun TimeZone.Companion.bsonDefinition() = bsonDefinition(
	parse = TimeZone::withId,
	serialize = TimeZone::toString
)


fun TimeZone.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::withId)

		parseJson(::withId)
		serializeJson(TimeZone::toString)
	}
}
