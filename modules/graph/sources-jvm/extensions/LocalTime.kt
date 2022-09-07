package io.fluidsonic.raptor.graph

import io.fluidsonic.time.*


public fun LocalTime.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(LocalTime::toString)
}
