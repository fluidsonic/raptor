package io.fluidsonic.raptor

import io.fluidsonic.time.*
import io.fluidsonic.time.LocalTime.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<LocalTime> {
	decode {
		reader.timestamp().toLocalTime(TimeZone.utc)
	}

	encode { value ->
		writer.value(value.atDate(LocalDate.firstIn1970).atTimeZone(TimeZone.utc))
	}
}
