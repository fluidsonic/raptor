// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("PreciseDuration@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun PreciseDuration.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::parse)

		parseJson(::parse)
		serializeJson(PreciseDuration::toString)
	}
}
