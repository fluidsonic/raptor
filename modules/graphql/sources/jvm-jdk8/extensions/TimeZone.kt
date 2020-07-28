// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("TimeZone@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun TimeZone.Companion.graphDefinition(): GraphScalarDefinition<TimeZone> = graphScalarDefinition {
	parseString(::withId)

	parseJson(::withId)
	serializeJson(TimeZone::toString)
}
