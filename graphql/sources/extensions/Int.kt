package io.fluidsonic.raptor


fun Int.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseInt(::identity)

		parseJson<Int>(::identity)
		serializeJson(::identity)
	}
}
