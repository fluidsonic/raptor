// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Double@graph")

package io.fluidsonic.raptor


fun Double.Companion.graphDefinition(): GraphScalarDefinition<Double> = graphScalarDefinition {
	parseFloat(::identity)

	parseJson(Number::toDouble) // FIXME truncation
	serializeJson(::identity)
}
