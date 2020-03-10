package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun TimeZone.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::withId)

		parseJson(::withId)
		serializeJson(TimeZone::toString)
	}
}
