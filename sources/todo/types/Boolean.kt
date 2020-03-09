package io.fluidsonic.raptor


fun Boolean.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseBoolean(::identity)

		parseJson<Boolean>(::identity)
		serializeJson(::identity)
	}
}
