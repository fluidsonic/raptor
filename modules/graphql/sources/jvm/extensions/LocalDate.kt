// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDate@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalDate.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }
	serialize(LocalDate::toString)
}
