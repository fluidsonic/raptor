// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Boolean@graph")

package io.fluidsonic.raptor


public fun Boolean.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseBoolean(::identity)
	serialize(::identity)
}
