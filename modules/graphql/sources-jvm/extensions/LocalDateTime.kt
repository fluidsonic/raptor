// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("LocalDateTime@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun LocalDateTime.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(LocalDateTime::toString)
}
