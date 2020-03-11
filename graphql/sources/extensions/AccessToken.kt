// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("AccessToken@graph")

package io.fluidsonic.raptor


fun AccessToken.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::AccessToken)

		parseJson(::AccessToken)
		serializeJson(AccessToken::value)
	}
}
