package io.fluidsonic.raptor.graph

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun TimeZone.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { ofOrNull(it) ?: invalid() }
	serialize(TimeZone::id)
}
