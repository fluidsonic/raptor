// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("PreciseDuration@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun PreciseDuration.Companion.graphDefinition(): GraphScalarDefinition<PreciseDuration> = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }

	parseJson<String> { parse(it) ?: invalid() }
	serializeJson(PreciseDuration::toString)
}
