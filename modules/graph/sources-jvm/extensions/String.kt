package io.fluidsonic.raptor.graph


public fun String.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString(::identity)
	serialize(::identity)
}
