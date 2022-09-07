package io.fluidsonic.raptor.graph


@Suppress("UnusedReceiverParameter")
public fun Unit.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Unit> {
	// outputOnly() // FIXME add this

	parseInt { TODO() }
	serialize { 42 } // FIXME
}
