// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Duration@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlin.time.*


@OptIn(ExperimentalTime::class)
public fun Duration.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(Duration::toIsoString)
}
