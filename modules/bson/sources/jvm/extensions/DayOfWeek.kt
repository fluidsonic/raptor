package io.fluidsonic.raptor

import io.fluidsonic.time.*
import io.fluidsonic.time.DayOfWeek.*


public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<DayOfWeek> {
	decode<String> { string ->
		when (string) {
			"monday" -> monday
			"tuesday" -> tuesday
			"wednesday" -> wednesday
			"thursday" -> thursday
			"friday" -> friday
			"saturday" -> saturday
			"sunday" -> sunday
			else -> error("invalid day of week: $string")
		}
	}

	encode<String> { value ->
		when (value) {
			monday -> "monday"
			tuesday -> "tuesday"
			wednesday -> "wednesday"
			thursday -> "thursday"
			friday -> "friday"
			saturday -> "saturday"
			sunday -> "sunday"
		}
	}
}
