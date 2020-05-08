// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("String@graph")

package io.fluidsonic.raptor


fun String.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::identity)

		parseJson<String>(::identity)
		serializeJson(::identity)
	}
}
