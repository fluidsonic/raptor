// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalTime.Companion.graphDefinition(): GraphScalarDefinition<LocalTime> = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }

	parseJson<String> { parse(it) ?: invalid() }
	serializeJson(LocalTime::toString)
}
