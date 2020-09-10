// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("CurrencyCode@bson")

package io.fluidsonic.raptor

import io.fluidsonic.currency.*
import io.fluidsonic.currency.CurrencyCode.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<CurrencyCode> {
	decode(::parse)
	encode(CurrencyCode::toString)
}
