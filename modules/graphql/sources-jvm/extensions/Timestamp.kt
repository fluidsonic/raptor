// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Timestamp@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun Instant.Companion.graphDefinitions(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(Timestamp::toString)
}
