// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("String@graph")

package io.fluidsonic.raptor


fun String.Companion.graphDefinition(): GraphScalarDefinition<String> = graphScalarDefinition {
	parseString(::identity)

	parseJson(::identity)
	serializeJson(::identity)
}
