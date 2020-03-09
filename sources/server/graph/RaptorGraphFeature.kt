package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import io.fluidsonic.time.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import org.kodein.di.erased.*


// FIXME We need a clear definition of the configuration execution order.
// FIXME Also, how to "complete" a configuration?
object RaptorGraphFeature : RaptorRouteFeature<RaptorGraphFeature.Builder> {

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


	override fun RaptorRouteConfigScope.configure(dslConfig: Builder.() -> Unit) {
		kodein {
			bind() from instance(BuilderImpl().apply(dslConfig).build())
		}

		ktor {
			get {
				dkodein.instance<GraphRoute>().handle(call)
			}

			post {
				dkodein.instance<GraphRoute>().handle(call)
			}
		}

		// FIXME
//		val graphRoute = GraphRoute(
//			system = GraphSystem(
//				definitions = configurations.flatMap { it.graphDefinitions }
//			)
//		)
	}


	interface Builder {

		fun graphDefinitions(vararg definitions: RaptorGraphDefinition) = graphDefinitions(definitions.asIterable())
		fun graphDefinitions(definitions: Iterable<RaptorGraphDefinition>)
	}


	private class BuilderImpl : Builder {

		private val definitions = defaultDefinitions.toMutableList()


		fun build() = Config(
			definitions = definitions
		)


		override fun graphDefinitions(definitions: Iterable<RaptorGraphDefinition>) {
			this.definitions += definitions
		}
	}


	private class Config(
		val definitions: List<RaptorGraphDefinition>
	)
}


fun RaptorRouteConfigScope.graph(config: RaptorGraphFeature.Builder.() -> Unit = {}) =
	install(RaptorGraphFeature, config = config)


fun RaptorServerConfigScope.graph(config: RaptorGraphFeature.Builder.() -> Unit = {}) =
	route(path = "graphql") {
		graph(config = config)
	}


fun RaptorServerConfigScope.graph(path: String, config: RaptorGraphFeature.Builder.() -> Unit = {}) =
	route(path = path) {
		graph(config = config)
	}
