// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalTime.Companion.graphDefinition(): GraphScalarDefinition<LocalTime> = graphScalarDefinition {
	parseString(::parse)

	parseJson(::parse)
	serializeJson(LocalTime::toString)
}
