package io.fluidsonic.raptor.graph


public fun Int.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseInt(::identity)
	serialize(::identity)
}
