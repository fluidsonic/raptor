package io.fluidsonic.raptor


fun PhoneNumber.Companion.bsonDefinition() = bsonDefinition(
	parse = ::PhoneNumber,
	serialize = PhoneNumber::value
)
