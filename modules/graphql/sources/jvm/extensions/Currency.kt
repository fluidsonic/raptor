// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Currency@graph")

package io.fluidsonic.raptor

import io.fluidsonic.currency.*


public fun Currency.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { forCodeOrNull(it) ?: invalid() }
	serialize(Currency::code)
}
