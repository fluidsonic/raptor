// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Cents@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Cents.Companion.graphDefinition(): GraphScalarDefinition<Cents> = graphScalarDefinition<Cents> {
	parseString { Cents(it.toLongOrNull() ?: invalid()) } // FIXME shouldn't need duplication

	parseJson<String> { Cents(it.toLongOrNull() ?: invalid()) }
	serializeJson { it.value.toString() }
}
