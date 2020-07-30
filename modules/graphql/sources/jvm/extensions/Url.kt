// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Url@graph")

package io.fluidsonic.raptor

import io.ktor.http.*


public fun Url.Companion.graphDefinition(): GraphScalarDefinition<Url> = graphScalarDefinition {
	parseString { runCatching { Url(it) }.getOrNull() ?: invalid() }

	parseJson<String> { runCatching { Url(it) }.getOrNull() ?: invalid() }
	serializeJson(Url::toString)
}
