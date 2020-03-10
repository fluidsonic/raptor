package io.fluidsonic.raptor


fun Password.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::Password)

		parseJson(::Password)
		serializeJson(Password::value)
	}
}
