package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlinx.datetime.*


public fun LocalDateTime.Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<LocalDateTime> {
	decode {
		reader.timestamp().toLocalDateTime(TimeZone.UTC)
	}
	encode { value ->
		writer.value(value.toTimestamp(TimeZone.UTC))
	}
}
