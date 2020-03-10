package io.fluidsonic.raptor


fun EmailAddress.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::EmailAddress)

		parseJson(::EmailAddress)
		serializeJson(EmailAddress::value)
	}
}
