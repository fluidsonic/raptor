package io.fluidsonic.raptor

import io.fluidsonic.time.*


fun LocalDateTime.Companion.bsonDefinition() = bsonDefinition<LocalDateTime> {
	decode {
		Timestamp.of(millisecondsSince1970 = Milliseconds(readDateTime())).toLocalDateTime(TimeZone.utc)
	}

	encode { value ->
		writeDateTime(value.atTimeZone(TimeZone.utc).millisecondsSince1970.toLong())
	}
}
