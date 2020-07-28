// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("CountryCode@graphql")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun CountryCode.Companion.graphDefinition(): GraphScalarDefinition<CountryCode> = graphScalarDefinition {
	parseString(::CountryCode)

	parseJson(::CountryCode)
	serializeJson(CountryCode::value)
}
