// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Url@graph")

package io.fluidsonic.raptor

import io.ktor.http.*


public fun Url.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { runCatching { Url(it) }.getOrNull() ?: invalid() }
	serialize(Url::toString)
}
