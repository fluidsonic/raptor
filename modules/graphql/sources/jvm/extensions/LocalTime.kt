// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalTime.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }
	serialize(LocalTime::toString)
}
