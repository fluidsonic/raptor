// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("TimeZone@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun TimeZone.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { withId(it) ?: invalid() }
	serialize(TimeZone::toString)
}
