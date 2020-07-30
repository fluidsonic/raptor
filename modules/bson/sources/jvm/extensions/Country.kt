// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@bson")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun Country.Companion.bsonDefinition() = bsonDefinition(
	parse = { byCode(CountryCode(it)) ?: throw BsonException("Invalid country code: $it") },
	serialize = { it.code.value }
)
