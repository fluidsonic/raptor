package io.fluidsonic.raptor

import io.fluidsonic.time.*
import io.fluidsonic.time.LocalDateTime.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<LocalDateTime> {
	decode {
		reader.timestamp().toLocalDateTime(TimeZone.utc)
	}

	encode { value ->
		writer.value(value.atTimeZone(TimeZone.utc))
	}
}
