package io.fluidsonic.raptor

import io.fluidsonic.time.*
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<LocalDate> {
	decode {
		reader.timestamp().toLocalDate(TimeZone.UTC)
	}
	encode { value ->
		writer.value(value.atStartOfDayIn(TimeZone.UTC))
	}
}
