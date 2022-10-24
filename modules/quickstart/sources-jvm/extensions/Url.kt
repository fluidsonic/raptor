@file:JvmName("Url@defaults")

package io.fluidsonic.raptor

import io.fluidsonic.raptor.bson.*
import io.ktor.http.*
import io.ktor.http.Url.*


@Suppress("RemoveExplicitTypeArguments")
public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Url> {
	decode<String>(::Url)
	encode(Url::toString)
}
