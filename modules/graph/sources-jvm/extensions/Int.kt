package io.fluidsonic.raptor.graph


public fun Int.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Int> {
	parseInt(::identity)
	serialize(::identity)
}
