package io.fluidsonic.raptor

import io.ktor.http.*


fun Url.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::Url)

		parseJson<String>(::Url)
		serializeJson(Url::toString)
	}
}
