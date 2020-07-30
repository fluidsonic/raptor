// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Timestamp@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun Timestamp.Companion.graphDefinitions(): GraphScalarDefinition<Timestamp> = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }

	parseJson<String> { parse(it) ?: invalid() }
	serializeJson(Timestamp::toString)
}
