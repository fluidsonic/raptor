@file:JvmName("Cents@extension")

package io.fluidsonic.raptor

import org.bson.*


internal fun Cents.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition<Cents> {
	decode {
		Cents(if (currentBsonType == BsonType.INT32) readInt32().toLong() else readInt64())
	}

	encode { value ->
		writeInt64(value.value)
	}
}

internal fun Cents.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Cents> {
	parseString { Cents(it.toLongOrNull() ?: invalid()) }
	serialize { it.value.toString() }
}
