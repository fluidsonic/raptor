// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Unit@graph")

package io.fluidsonic.raptor


@Suppress("unused")
public fun Unit.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Unit> {
	// outputOnly() // FIXME add this

	parseInt { TODO() }
	serialize { 42 } // FIXME
}
