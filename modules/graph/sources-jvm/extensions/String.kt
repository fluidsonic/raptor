package io.fluidsonic.raptor.graph


public fun String.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<String> {
	parseString(::identity)
	serialize(::identity)
}
