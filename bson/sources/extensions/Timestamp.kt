package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun Timestamp.Companion.bsonDefinition() = bsonDefinition<Timestamp> {
	decode {
		of(millisecondsSince1970 = Milliseconds(readDateTime()))
	}

	encode { value ->
		writeDateTime(value.millisecondsSince1970.toLong())
	}
}
