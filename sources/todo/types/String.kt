package io.fluidsonic.raptor


fun String.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::identity)

		parseJson<String>(::identity)
		serializeJson(::identity)
	}
}
