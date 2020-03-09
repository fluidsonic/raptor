package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun Country.Companion.bsonDefinition() = bsonDefinition(
	parse = { byCode(it) ?: throw BsonException("Invalid country code: $it") },
	serialize = Country::code
)


// FIXME don't serialize as ID
fun Country.Companion.graphDefinition() = graphScalarDefinition {
	conversion {
		parseString(::byCode)

		parseJson(::byCode)
		serializeJson(Country::code)
	}
}
