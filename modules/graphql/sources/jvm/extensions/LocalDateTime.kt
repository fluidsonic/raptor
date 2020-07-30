// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDateTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalDateTime.Companion.graphDefinition(): GraphScalarDefinition<LocalDateTime> = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }

	parseJson<String> { parse(it) ?: invalid() }
	serializeJson(LocalDateTime::toString)
}
