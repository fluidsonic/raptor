// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("String@graph")

package io.fluidsonic.raptor


public fun String.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString(::identity)
	serialize(::identity)
}
