package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalDate.Companion.bsonDefinition() = bsonDefinition(
	parse = LocalDate::parse,
	serialize = LocalDate::toString
)


fun LocalDate.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::parse)

		parseJson(::parse)
		serializeJson(LocalDate::toString)
	}
}
