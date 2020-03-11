// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("PhoneNumber@graph")

package io.fluidsonic.raptor


fun PhoneNumber.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::PhoneNumber)

		parseJson(::PhoneNumber)
		serializeJson(PhoneNumber::value)
	}
}
