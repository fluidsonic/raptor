// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDate@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun LocalDate.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(LocalDate::toString)
}
