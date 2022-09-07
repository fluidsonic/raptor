package io.fluidsonic.raptor.graph


public fun Double.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition(name = "Float") {
	parseFloat(::identity)
	serialize(::identity)
}
