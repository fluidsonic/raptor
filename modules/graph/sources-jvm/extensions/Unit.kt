package io.fluidsonic.raptor.graph


@Suppress("UnusedReceiverParameter")
public fun Unit.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Unit> {
	// outputOnly() // TODO add this

	parseInt { TODO() }
	serialize { 42 } // TODO
}
