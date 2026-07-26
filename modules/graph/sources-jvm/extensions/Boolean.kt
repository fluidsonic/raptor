package io.fluidsonic.raptor.graph

import kotlin.reflect.*


public fun Boolean.Companion.graphDefinition(): RaptorGraphDefinition =
	graphUncoercedScalarDefinition(type = typeOf<Boolean>())
