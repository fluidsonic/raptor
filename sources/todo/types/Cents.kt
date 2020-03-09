package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import org.bson.*


fun Cents.Companion.bsonDefinition() = bsonDefinition<Cents> {
	decode {
		Cents(if (currentBsonType == BsonType.INT32) readInt32().toLong() else readInt64())
	}

	encode { value ->
		writeInt64(value.value)
	}
}


fun Cents.Companion.graphDefinition() = graphScalarDefinition {
	conversion<Cents> {
		parseString { it.toLongOrNull()?.let(::Cents) }

		parseJson<String> { it.toLongOrNull()?.let(::Cents) }
		serializeJson { it.value.toString() }
	}
}
