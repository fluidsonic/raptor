package io.fluidsonic.raptor.graph

import io.fluidsonic.country.*
import io.fluidsonic.currency.*
import io.fluidsonic.locale.*
import io.fluidsonic.time.*
import kotlin.time.*
import kotlinx.datetime.*


public object RaptorGraphDefaults {

	public val definitions: Collection<RaptorGraphDefinition> = listOf(
		Country.graphDefinition(),
		CountryCode.graphDefinition(),
		Currency.graphDefinition(),
		Duration.graphDefinition(),
		LocalDate.graphDefinition(),
		LocalDateTime.graphDefinition(),
		LocalTime.graphDefinition(),
		Locale.graphDefinition(),
		Timestamp.graphDefinition(),
		TimeZone.graphDefinition(),
		Unit.graphDefinition(),
	)
}
