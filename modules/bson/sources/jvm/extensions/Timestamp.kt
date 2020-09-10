package io.fluidsonic.raptor

import io.fluidsonic.time.*
import io.fluidsonic.time.Timestamp.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Timestamp> {
	decode {
		reader.timestamp()
	}

	encode { value ->
		writer.value(value)
	}
}
