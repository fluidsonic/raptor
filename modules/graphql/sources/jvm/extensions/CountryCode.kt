// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("CountryCode@graphql")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun CountryCode.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString(::CountryCode)
	serialize(CountryCode::value)
}
