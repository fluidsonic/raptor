// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@graphql")

package io.fluidsonic.raptor

import io.fluidsonic.country.*
import io.fluidsonic.i18n.*
import io.fluidsonic.locale.*


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
