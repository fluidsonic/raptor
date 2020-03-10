package io.fluidsonic.raptor


fun PasswordHash.Companion.bsonDefinition() = bsonDefinition(
	parse = ::PasswordHash,
	serialize = PasswordHash::value
)
