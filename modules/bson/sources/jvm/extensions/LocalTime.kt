package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalTime.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition<LocalTime> {
	decode {
		Timestamp.of(millisecondsSince1970 = Milliseconds(readDateTime())).toLocalTime(TimeZone.utc)
	}

	encode { value ->
		writeDateTime(value.atDate(LocalDate.firstIn1970).atTimeZone(TimeZone.utc).millisecondsSince1970.toLong())
	}
}
