// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Locale@graph")

package io.fluidsonic.raptor

import io.fluidsonic.locale.*


// TODO move to fluid-stdlib
internal val Locale.code: String
	get() = toPlatform().toLanguageTag()


public fun Locale.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { forCodeOrNull(it) ?: invalid() } // TODO shouldn't need duplication
	serialize(Locale::code)
}


// TODO move to fluid-stdlib
internal fun Locale.Companion.forCodeOrNull(code: String): Locale? =
	runCatching { java.util.Locale.forLanguageTag(code).toCommon() }.getOrNull()
