// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Currency@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Currency.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { byCode(it) ?: invalid() }
	serialize(Currency::code)
}
