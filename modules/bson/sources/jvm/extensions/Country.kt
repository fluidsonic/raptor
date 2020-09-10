// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@bson")

package io.fluidsonic.raptor

import io.fluidsonic.country.*
import io.fluidsonic.country.Country.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Country> {
	decode<CountryCode>(::forCode)
	encode(Country::code)
}
