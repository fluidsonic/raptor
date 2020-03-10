package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalDate.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::parse)

		parseJson(::parse)
		serializeJson(LocalDate::toString)
	}
}
