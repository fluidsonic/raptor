package io.fluidsonic.raptor.graph

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun LocalDate.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<LocalDate> {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(LocalDate::toString)
}
