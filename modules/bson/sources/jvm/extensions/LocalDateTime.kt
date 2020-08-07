package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun LocalDateTime.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition<LocalDateTime> {
	decode {
		Timestamp.of(millisecondsSince1970 = Milliseconds(readDateTime())).toLocalDateTime(TimeZone.utc)
	}

	encode { value ->
		writeDateTime(value.atTimeZone(TimeZone.utc).millisecondsSince1970.toLong())
	}
}
