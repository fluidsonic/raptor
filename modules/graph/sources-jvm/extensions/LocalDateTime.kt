package io.fluidsonic.raptor.graph

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun LocalDateTime.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(LocalDateTime::toString)
}
