package io.fluidsonic.raptor.graph


public fun Boolean.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseBoolean(::identity)
	serialize(::identity)
}
