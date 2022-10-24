package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlinx.datetime.Instant.*


@Suppress("RemoveExplicitTypeArguments")
public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Timestamp> {
	decode {
		reader.timestamp()
	}
	encode { value ->
		writer.value(value)
	}
}
