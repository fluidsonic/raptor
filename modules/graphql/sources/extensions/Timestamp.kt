// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Timestamp@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun Timestamp.Companion.graphDefinitions(): GraphScalarDefinition<Timestamp> = graphScalarDefinition {
	parseString(::parse)

	parseJson(::parse)
	serializeJson(Timestamp::toString)
}
