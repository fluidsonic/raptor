// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Currency@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Currency.Companion.graphDefinition(): GraphScalarDefinition<Currency> = graphScalarDefinition {
	parseString { byCode(it) ?: invalid() }

	parseJson<String> { byCode(it) ?: invalid() }
	serializeJson(Currency::code)
}
