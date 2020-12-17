// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("TimeZone@graph")

package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun TimeZone.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { ofOrNull(it) ?: invalid() }
	serialize(TimeZone::id)
}
