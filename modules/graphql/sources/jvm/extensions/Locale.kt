// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Locale@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


// FIXME move to fluid-stdlib
internal val Locale.code: String
	get() = toPlatform().toLanguageTag()


public fun Locale.Companion.graphDefinition(): GraphScalarDefinition<Locale> = graphScalarDefinition {
	parseString { forCodeOrNull(it) ?: invalid() } // FIXME shouldn't need duplication

	parseJson<String> { forCodeOrNull(it) ?: invalid() }
	serializeJson { it.code }
}


// FIXME move to fluid-stdlib
internal fun Locale.Companion.forCodeOrNull(code: String): Locale? =
	runCatching { java.util.Locale.forLanguageTag(code).toCommon() }.getOrNull()
