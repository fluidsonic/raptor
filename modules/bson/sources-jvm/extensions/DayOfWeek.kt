package io.fluidsonic.raptor

import java.time.*


@Suppress("FunctionName")
public fun DayOfWeek_bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<DayOfWeek> {
	decode<String> { string ->
		when (string) {
			"monday" -> DayOfWeek.MONDAY
			"tuesday" -> DayOfWeek.TUESDAY
			"wednesday" -> DayOfWeek.WEDNESDAY
			"thursday" -> DayOfWeek.THURSDAY
			"friday" -> DayOfWeek.FRIDAY
			"saturday" -> DayOfWeek.SATURDAY
			"sunday" -> DayOfWeek.SUNDAY
			else -> error("invalid day of week: $string")
		}
	}

	encode<String> { value ->
		when (value) {
			DayOfWeek.MONDAY -> "monday"
			DayOfWeek.TUESDAY -> "tuesday"
			DayOfWeek.WEDNESDAY -> "wednesday"
			DayOfWeek.THURSDAY -> "thursday"
			DayOfWeek.FRIDAY -> "friday"
			DayOfWeek.SATURDAY -> "saturday"
			DayOfWeek.SUNDAY -> "sunday"
		}
	}
}

