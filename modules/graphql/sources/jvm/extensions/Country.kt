// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@graphql")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Country.Companion.graphDefinition(): GraphObjectDefinition<Country> = graphObjectDefinition<Country> {
	field(Country::code)
	field("name") {
		val locale by argument<Locale> {
			defaultString("en-US")
		}

		resolver { country ->
			country.name(locale)
		}
	}
}
