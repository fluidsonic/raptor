// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("CountryCode@bson")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun CountryCode.Companion.bsonDefinition(): RaptorBsonDefinition<CountryCode> = bsonDefinition(
	parse = ::CountryCode,
	serialize = CountryCode::value
)
