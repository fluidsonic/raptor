package io.fluidsonic.raptor

import io.fluidsonic.currency.*
import io.fluidsonic.currency.Currency.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Currency> {
	decode<CurrencyCode>(::forCode)
	encode(Currency::code)
}
