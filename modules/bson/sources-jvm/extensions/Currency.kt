package io.fluidsonic.raptor.bson

import io.fluidsonic.currency.*
import io.fluidsonic.currency.Currency.*
import io.fluidsonic.raptor.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Currency> {
	decode<CurrencyCode>(::forCode)
	encode(Currency::code)
}
