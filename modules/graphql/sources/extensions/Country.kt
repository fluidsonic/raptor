// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


// FIXME don't serialize as ID
fun Country.Companion.graphDefinition(): GraphScalarDefinition<Country> = graphScalarDefinition {
	parseString(::byCode)

	parseJson(::byCode)
	serializeJson(Country::code)
}
