package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalDate.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition<LocalDate> {
	decode {
		Timestamp.of(millisecondsSince1970 = Milliseconds(readDateTime())).toLocalDate(TimeZone.utc)
	}

	encode { value ->
		writeDateTime(value.atStartOfDay(TimeZone.utc).millisecondsSince1970.toLong())
	}
}
