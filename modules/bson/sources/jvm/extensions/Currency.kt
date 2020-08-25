package io.fluidsonic.raptor

import io.fluidsonic.currency.*


public fun Currency.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = Currency::forCode,
	serialize = { it.code.toString() }
)
