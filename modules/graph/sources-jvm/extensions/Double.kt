package io.fluidsonic.raptor.graph

import kotlin.reflect.*


public fun Double.Companion.graphDefinition(): RaptorGraphDefinition =
	graphUncoercedScalarDefinition(name = "Float", type = typeOf<Double>())
