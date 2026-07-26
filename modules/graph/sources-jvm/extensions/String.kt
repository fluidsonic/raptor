package io.fluidsonic.raptor.graph

import kotlin.reflect.*


public fun String.Companion.graphDefinition(): RaptorGraphDefinition =
	graphUncoercedScalarDefinition(type = typeOf<String>())
