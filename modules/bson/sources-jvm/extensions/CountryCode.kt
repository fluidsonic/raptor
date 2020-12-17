// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("CountryCode@bson")

package io.fluidsonic.raptor

import io.fluidsonic.country.*
import io.fluidsonic.country.CountryCode.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<CountryCode> {
	decode(::parse)
	encode(CountryCode::toString)
}
