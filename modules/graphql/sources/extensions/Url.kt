// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Url@graph")

package io.fluidsonic.raptor

import io.ktor.http.*


fun Url.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::Url)

		parseJson<String>(::Url)
		serializeJson(Url::toString)
	}
}
