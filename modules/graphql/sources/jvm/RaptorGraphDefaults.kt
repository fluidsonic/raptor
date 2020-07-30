package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import io.ktor.http.*


object RaptorGraphDefaults {

	val definitions = listOf<RaptorGraphDefinition>(
		Cents.graphDefinition(),
		Country.graphDefinition(),
		CountryCode.graphDefinition(),
		Currency.graphDefinition(),
		LocalDate.graphDefinition(),
		LocalDateTime.graphDefinition(),
		LocalTime.graphDefinition(),
		Money.graphDefinition(),
		PreciseDuration.graphDefinition(),
		Timestamp.graphDefinitions(),
		TimeZone.graphDefinition(),
		Unit.graphDefinition(),
		Url.graphDefinition()
	)
}
