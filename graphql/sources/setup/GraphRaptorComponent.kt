package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import io.ktor.http.*


@Raptor.Dsl3
class GraphRaptorComponent internal constructor(
	override val raptorTags: Set<Any>
) : RaptorComponent.Taggable {

	internal val definitions = defaultDefinitions.toMutableList()


	companion object {

		private val defaultDefinitions = listOf<RaptorGraphDefinition>(
			AccessToken.graphDefinition(),
			Cents.graphDefinition(),
			Country.graphDefinition(),
			Currency.graphDefinition(),
			EmailAddress.graphDefinition(),
			LocalDate.graphDefinition(),
			LocalTime.graphDefinition(),
			Money.graphDefinition(),
			Password.graphDefinition(),
			PreciseDuration.graphDefinition(),
			PhoneNumber.graphDefinition(),
			Timestamp.graphDefinitions(),
			TimeZone.graphDefinition(),
			Unit.graphDefinition(),
			Url.graphDefinition()
		)
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<GraphRaptorComponent>.definitions(vararg definitions: RaptorGraphDefinition) {
	definitions(definitions.asIterable())

}


@Raptor.Dsl3
fun RaptorConfigurable<GraphRaptorComponent>.definitions(definitions: Iterable<RaptorGraphDefinition>) {
	raptorComponentConfiguration {
		this.definitions += definitions
	}
}

