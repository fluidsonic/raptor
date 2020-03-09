package io.fluidsonic.raptor


fun Double.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseFloat(::identity)

		parseJson<Number> { it.toDouble() } // FIXME truncation
		serializeJson(::identity)
	}
}
