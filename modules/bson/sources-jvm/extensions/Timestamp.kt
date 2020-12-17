package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlinx.datetime.Instant.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Timestamp> {
	decode {
		reader.timestamp()
	}
	encode { value ->
		writer.value(value)
	}
}
