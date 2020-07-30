// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Unit@graph")

package io.fluidsonic.raptor


@Suppress("unused")
public fun Unit.graphDefinition(): GraphScalarDefinition<Unit> = graphScalarDefinition<Unit> {
	// outputOnly() // FIXME add this

	parseInt { TODO() }
	parseJson<Any> { TODO() }
	serializeJson { 42 } // FIXME
}
