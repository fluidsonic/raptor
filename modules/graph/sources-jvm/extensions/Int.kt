package io.fluidsonic.raptor.graph

import kotlin.reflect.*


public fun Int.Companion.graphDefinition(): RaptorGraphDefinition =
	graphUncoercedScalarDefinition(type = typeOf<Int>())
