package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun Timestamp.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition<Timestamp> {
	decode {
		of(millisecondsSince1970 = Milliseconds(readDateTime()))
	}

	encode { value ->
		writeDateTime(value.millisecondsSince1970.toLong())
	}
}
