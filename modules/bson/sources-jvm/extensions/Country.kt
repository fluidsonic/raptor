package io.fluidsonic.raptor.bson

import io.fluidsonic.country.*
import io.fluidsonic.country.Country.*
import io.fluidsonic.raptor.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Country> {
	decode<CountryCode>(::forCode)
	encode(Country::code)
}
