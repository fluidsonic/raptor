@file:JvmName("Url@defaults")

package io.fluidsonic.raptor

import io.ktor.http.*


fun Url.Companion.bsonDefinition() = bsonDefinition(
	parse = ::Url,
	serialize = Url::toString
)
