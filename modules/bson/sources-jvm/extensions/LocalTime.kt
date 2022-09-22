package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlinx.datetime.*


private val referenceDate = LocalDate(1970, 1, 1)


public fun LocalTime.Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition {
	decode {
		reader.timestamp().toLocalTime(TimeZone.UTC)
	}
	encode { value ->
		writer.value(value.atDate(referenceDate).toTimestamp(TimeZone.UTC))
	}
}
