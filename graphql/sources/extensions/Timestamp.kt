package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun Timestamp.Companion.graphDefinitions() = graphScalarDefinition {
	conversion {
		parseString(::parse)

		parseJson(::parse)
		serializeJson(Timestamp::toString)
	}
}
