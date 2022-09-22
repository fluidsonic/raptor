package io.fluidsonic.raptor.graph

import io.fluidsonic.time.*


// TODO For some reason IDEA removes the kotlinx.datetime.* import when optimizing imports.
public fun kotlinx.datetime.LocalTime.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(kotlinx.datetime.LocalTime::toString)
}
