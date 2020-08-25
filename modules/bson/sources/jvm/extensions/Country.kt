// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Country@bson")

package io.fluidsonic.raptor

import io.fluidsonic.country.*


public fun Country.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = Country::forCode,
	serialize = { it.code.toString() }
)
