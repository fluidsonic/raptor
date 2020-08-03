// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Double@graph")

package io.fluidsonic.raptor


public fun Double.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseFloat(::identity)
	serialize(::identity)
}
