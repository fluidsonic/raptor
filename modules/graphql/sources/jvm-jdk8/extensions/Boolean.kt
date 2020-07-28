// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Boolean@graph")

package io.fluidsonic.raptor


fun Boolean.Companion.graphDefinition(): GraphScalarDefinition<Boolean> = graphScalarDefinition {
	parseBoolean(::identity)

	parseJson(::identity)
	serializeJson(::identity)
}
