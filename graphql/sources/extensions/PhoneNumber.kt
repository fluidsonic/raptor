package io.fluidsonic.raptor


fun PhoneNumber.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::PhoneNumber)

		parseJson(::PhoneNumber)
		serializeJson(PhoneNumber::value)
	}
}
