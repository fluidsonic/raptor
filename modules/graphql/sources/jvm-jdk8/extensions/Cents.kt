// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Cents@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun Cents.Companion.graphDefinition(): GraphScalarDefinition<Cents> = graphScalarDefinition<Cents> {
	parseString { it.toLongOrNull()?.let(::Cents) }

	parseJson<String> { it.toLongOrNull()?.let(::Cents) }
	serializeJson { it.value.toString() }
}
