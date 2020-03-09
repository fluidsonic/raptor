package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun Currency.Companion.bsonDefinition() = bsonDefinition(
	parse = { byCode(it) ?: throw BsonException("Invalid currency code: $it") },
	serialize = Currency::code
)


fun Currency.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::byCode)

		parseJson(::byCode)
		serializeJson(Currency::code)
	}
}
