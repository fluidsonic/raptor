package io.fluidsonic.raptor.bson

import io.fluidsonic.currency.*
import io.fluidsonic.currency.CurrencyCode.*
import io.fluidsonic.raptor.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<CurrencyCode> {
	decode(::parse)
	encode(CurrencyCode::toString)
}
