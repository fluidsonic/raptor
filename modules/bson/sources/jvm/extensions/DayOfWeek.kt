package io.fluidsonic.raptor

import io.fluidsonic.time.*


public fun DayOfWeek.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition(
	parse = { string ->
		when (string) {
			"monday" -> DayOfWeek.monday
			"tuesday" -> DayOfWeek.tuesday
			"wednesday" -> DayOfWeek.wednesday
			"thursday" -> DayOfWeek.thursday
			"friday" -> DayOfWeek.friday
			"saturday" -> DayOfWeek.saturday
			"sunday" -> DayOfWeek.sunday
			else -> error("invalid day of week: $string")
		}
	},
	serialize = { value ->
		when (value) {
			DayOfWeek.monday -> "monday"
			DayOfWeek.tuesday -> "tuesday"
			DayOfWeek.wednesday -> "wednesday"
			DayOfWeek.thursday -> "thursday"
			DayOfWeek.friday -> "friday"
			DayOfWeek.saturday -> "saturday"
			DayOfWeek.sunday -> "sunday"
		}
	}
)
