// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("EmailAddress@graph")

package io.fluidsonic.raptor


fun EmailAddress.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::EmailAddress)

		parseJson(::EmailAddress)
		serializeJson(EmailAddress::value)
	}
}
