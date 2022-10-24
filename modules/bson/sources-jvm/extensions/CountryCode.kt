package io.fluidsonic.raptor.bson

import io.fluidsonic.country.*
import io.fluidsonic.country.CountryCode.*
import io.fluidsonic.raptor.*


@Suppress("RemoveExplicitTypeArguments")
public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<CountryCode> {
	decode(::parse)
	encode(CountryCode::toString)
}
