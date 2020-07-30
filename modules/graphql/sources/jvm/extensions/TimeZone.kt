// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("TimeZone@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun TimeZone.Companion.graphDefinition(): GraphScalarDefinition<TimeZone> = graphScalarDefinition {
	parseString { withId(it) ?: invalid() }

	parseJson<String> { withId(it) ?: invalid() }
	serializeJson(TimeZone::toString)
}
