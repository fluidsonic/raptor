package io.fluidsonic.raptor


fun AccessToken.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::AccessToken)

		parseJson(::AccessToken)
		serializeJson(AccessToken::value)
	}
}
