package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import org.bson.*


public fun Cents.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition<Cents> {
	decode {
		Cents(if (currentBsonType == BsonType.INT32) readInt32().toLong() else readInt64())
	}

	encode { value ->
		writeInt64(value.value)
	}
}
