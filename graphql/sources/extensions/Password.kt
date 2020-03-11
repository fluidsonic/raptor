// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Password@graph")

package io.fluidsonic.raptor


fun Password.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::Password)

		parseJson(::Password)
		serializeJson(Password::value)
	}
}
