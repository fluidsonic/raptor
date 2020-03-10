package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalTime.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::parse)

		parseJson(::parse)
		serializeJson(LocalTime::toString)
	}
}
