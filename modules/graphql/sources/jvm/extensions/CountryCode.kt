// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("CountryCode@graphql")

package io.fluidsonic.raptor

import io.fluidsonic.country.*


public fun CountryCode.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { parseOrNull(it) ?: invalid() }
	serialize(CountryCode::toString)
}
