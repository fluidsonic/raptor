// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Cents@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Cents.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Cents> {
	parseString { Cents(it.toLongOrNull() ?: invalid()) }
	serialize { it.value.toString() }
}
