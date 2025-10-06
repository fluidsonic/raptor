package io.fluidsonic.raptor.graph


public fun Double.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Double>(name = "Float") {
	parseFloat(::identity)
	serialize(::identity)
}
