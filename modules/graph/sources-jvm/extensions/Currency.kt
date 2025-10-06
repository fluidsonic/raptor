package io.fluidsonic.raptor.graph

import io.fluidsonic.currency.*


public fun Currency.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Currency> {
	parseString { forCodeOrNull(it) ?: invalid() }
	serialize(Currency::code)
}
