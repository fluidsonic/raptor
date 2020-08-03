// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDateTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalDateTime.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parse(it) ?: invalid() }
	serialize(LocalDateTime::toString)
}
