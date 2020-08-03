// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("PreciseDuration@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun PreciseDuration.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }
	serialize(PreciseDuration::toString)
}
