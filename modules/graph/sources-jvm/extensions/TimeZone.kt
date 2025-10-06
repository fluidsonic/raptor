package io.fluidsonic.raptor.graph

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun TimeZone.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<TimeZone> {
	parseString { ofOrNull(it) ?: invalid() }
	serialize(TimeZone::id)
}
