package io.fluidsonic.raptor.graph

import io.fluidsonic.locale.*


public fun Locale.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Locale> {
	parseString { forLanguageTagOrNull(it) ?: invalid() }
	serialize(Locale::toString)
}
