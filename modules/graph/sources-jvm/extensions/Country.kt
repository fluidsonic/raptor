package io.fluidsonic.raptor.graph

import io.fluidsonic.country.*
import io.fluidsonic.i18n.*
import io.fluidsonic.locale.*


@Suppress("RemoveExplicitTypeArguments")
public fun Country.Companion.graphDefinition(): RaptorGraphDefinition = graphObjectDefinition<Country> {
	field(Country::code)
	field("name") {
		val locale by argument<Locale> {
			defaultString("en-US")
		}

		resolver { country ->
			country.name(locale) ?: country.name
		}
	}
}
