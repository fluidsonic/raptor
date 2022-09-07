package io.fluidsonic.raptor.graph

import io.fluidsonic.locale.*


// FIXME move to fluid-stdlib
internal val Locale.code: String
	get() = toPlatform().toLanguageTag()


public fun Locale.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseString { forCodeOrNull(it) ?: invalid() } // FIXME shouldn't need duplication
	serialize(Locale::code)
}


// FIXME move to fluid-stdlib
internal fun Locale.Companion.forCodeOrNull(code: String): Locale? =
	runCatching { java.util.Locale.forLanguageTag(code).toCommon() }.getOrNull()
