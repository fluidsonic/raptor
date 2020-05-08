package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun Country.Companion.bsonDefinition() = bsonDefinition(
	parse = { byCode(it) ?: throw BsonException("Invalid country code: $it") },
	serialize = Country::code
)
