package io.fluidsonic.raptor

import io.fluidsonic.locale.*
import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import io.ktor.http.*


public object RaptorGraphDefaults {

	public val definitions: Collection<RaptorGraphDefinition> = listOf<RaptorGraphDefinition>(
		Cents.graphDefinition(),
		Country.graphDefinition(),
		CountryCode.graphDefinition(),
		Currency.graphDefinition(),
		LocalDate.graphDefinition(),
		LocalDateTime.graphDefinition(),
		LocalTime.graphDefinition(),
		Locale.graphDefinition(),
		Money.graphDefinition(),
		PreciseDuration.graphDefinition(),
		Timestamp.graphDefinitions(),
		TimeZone.graphDefinition(),
		Unit.graphDefinition(),
		Url.graphDefinition()
	)
}
