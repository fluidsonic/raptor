// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun kotlinx.datetime.LocalTime.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(kotlinx.datetime.LocalTime::toString)
}
