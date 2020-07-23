// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@graphql")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import java.util.Locale as PlatformLocale


fun Country.Companion.graphDefinition(): GraphObjectDefinition<Country> = graphObjectDefinition<Country> {
	field(Country::code)
	field<String>("name") {
		val locale by argument<String> {
			defaultString("en-US")
		}

		resolver { country ->
			country.name(Locale(PlatformLocale.forLanguageTag(locale)))
		}
	}
}
