// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDate@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalDate.Companion.graphDefinition(): GraphScalarDefinition<LocalDate> = graphScalarDefinition {
	parseString(::parse)

	parseJson(::parse)
	serializeJson(LocalDate::toString)
}
