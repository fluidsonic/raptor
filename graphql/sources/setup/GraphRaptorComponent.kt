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
			Cents.graphDefinition(),
			Country.graphDefinition(),
			Currency.graphDefinition(),
			LocalDate.graphDefinition(),
			LocalTime.graphDefinition(),
			Money.graphDefinition(),
			PreciseDuration.graphDefinition(),
			Timestamp.graphDefinitions(),
			TimeZone.graphDefinition(),
			Unit.graphDefinition(),
			Url.graphDefinition()
		)
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<GraphRaptorComponent>.definitions(vararg definitions: RaptorGraphDefinition) {
	definitions(definitions.asIterable())

}


@Raptor.Dsl3
fun RaptorComponentScope<GraphRaptorComponent>.definitions(definitions: Iterable<RaptorGraphDefinition>) {
	raptorComponentSelection {
		component.definitions += definitions
	}
}

