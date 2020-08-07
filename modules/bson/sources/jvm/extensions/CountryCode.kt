// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("CountryCode@bson")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun CountryCode.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = ::CountryCode,
	serialize = CountryCode::value
)
