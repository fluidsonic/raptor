package io.fluidsonic.raptor.graph


public fun Boolean.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Boolean> {
	parseBoolean(::identity)
	serialize(::identity)
}
