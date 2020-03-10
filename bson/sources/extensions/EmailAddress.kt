package io.fluidsonic.raptor


fun EmailAddress.Companion.bsonDefinition() = bsonDefinition(
	parse = ::EmailAddress,
	serialize = EmailAddress::value
)
