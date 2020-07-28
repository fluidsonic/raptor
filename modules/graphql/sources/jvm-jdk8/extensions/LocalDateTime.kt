// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDateTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalDateTime.Companion.graphDefinition(): GraphScalarDefinition<LocalDateTime> = graphScalarDefinition {
	parseString(::parse)

	parseJson(::parse)
	serializeJson(LocalDateTime::toString)
}
