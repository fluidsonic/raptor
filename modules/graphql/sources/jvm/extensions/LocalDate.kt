// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDate@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalDate.Companion.graphDefinition(): GraphScalarDefinition<LocalDate> = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }

	parseJson<String> { parse(it) ?: invalid() }
	serializeJson(LocalDate::toString)
}
