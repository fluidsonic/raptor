package io.fluidsonic.raptor.graph

import io.fluidsonic.country.*


public fun CountryCode.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(CountryCode::toString)
}
