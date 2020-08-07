// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@bson")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Country.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = { byCode(CountryCode(it)) ?: throw BsonException("Invalid country code: $it") },
	serialize = { it.code.value }
)
