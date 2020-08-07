package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Currency.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = { byCode(it) ?: throw BsonException("Invalid currency code: $it") },
	serialize = Currency::code
)
