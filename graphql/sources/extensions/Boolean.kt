// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Boolean@graph")

package io.fluidsonic.raptor


fun Boolean.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseBoolean(::identity)

		parseJson<Boolean>(::identity)
		serializeJson(::identity)
	}
}
