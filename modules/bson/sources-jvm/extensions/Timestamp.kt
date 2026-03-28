package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlin.time.*


public fun Instant.Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Timestamp> {
	decode {
		reader.timestamp()
	}
	encode { value ->
		writer.value(value)
	}
}
