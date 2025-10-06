package io.fluidsonic.raptor.graph

import kotlin.time.*


public fun Duration.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Duration> {
	parseString { parseIsoStringOrNull(it) ?: invalid() }
	serialize(Duration::toIsoString)
}
