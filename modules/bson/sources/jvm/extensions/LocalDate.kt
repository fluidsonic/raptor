package io.fluidsonic.raptor

import io.fluidsonic.time.*
import io.fluidsonic.time.LocalDate.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<LocalDate> {
	decode {
		reader.timestamp().toLocalDate(TimeZone.utc)
	}

	encode { value ->
		writer.value(value.atStartOfDay(TimeZone.utc))
	}
}
