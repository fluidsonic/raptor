// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Duration@graph")

package io.fluidsonic.raptor

import kotlin.time.*


public fun Duration.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseIsoStringOrNull(it) ?: invalid() }
	serialize(Duration::toIsoString)
}
