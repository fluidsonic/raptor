package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.raptor.graph.*
import io.ktor.http.*


public fun Url.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { runCatching { Url(it) }.getOrNull() ?: invalid() }
	serialize(Url::toString)
}
