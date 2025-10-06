package io.fluidsonic.raptor.graph

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun Instant.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Timestamp>("Timestamp") {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(Timestamp::toString)
}
