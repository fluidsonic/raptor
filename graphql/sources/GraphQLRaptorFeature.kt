package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import io.ktor.http.*


// FIXME Also, how to "complete" a configuration?
// FIXME does this have to be a feature?
object GraphQLRaptorFeature : RaptorKtorRouteFeature {

	val defaultDefinitions = listOf<RaptorGraphDefinition>(
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


	override fun RaptorFeatureSetup.setup(scope: RaptorKtorRouteComponent) {
		raptorSetupContext.register(GraphRaptorComponent())

//		custom {
//			get {
//				dkodein.instance<GraphRoute>().handle(call)
//			}
//
//			post {
//				dkodein.instance<GraphRoute>().handle(call)
//			}
//		}

		// FIXME
//		val graphRoute = GraphRoute(
//			system = GraphSystem(
//				definitions = configurations.flatMap { it.graphDefinitions }
//			)
//		)
	}
}


@Raptor.Dsl3
val RaptorFeatureSetup.graph
	get(): RaptorConfigurableCollection<GraphRaptorComponent> {
		return raptorSetupContext.getComponentCollection<GraphRaptorComponent>()
	}
