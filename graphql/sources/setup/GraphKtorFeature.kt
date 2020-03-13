package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.routing.*
import org.kodein.di.erased.*


object GraphKtorFeature : KtorRouteFeature {

	override fun KtorRouteFeatureSetup.setup() {
		// FIXME scope to local and add global as option?!

		route {
			custom {
				get {
					instance<GraphRoute>().handle(call)
				}

				post {
					instance<GraphRoute>().handle(call)
				}
			}
		}
	}


	override fun KtorRouteFeatureSetupCompletion.completeSetup() {
		// FIXME scope to local and add global as option?!

		route {
			val definitions = componentRegistry.getSingle<GraphRaptorComponent>()
				?.component
				?.definitions
				?.toList()
				?: return@route

			kodein {
				bind() from singleton {
					GraphRoute(
						system = GraphSystem(definitions = definitions)
					)
				}
			}
		}
	}
}


@Raptor.Dsl3
val RaptorComponentScope<RaptorFeatureComponent>.graphs: RaptorComponentScope.Collection<GraphRaptorComponent>
	get() = raptorComponentSelection.map { registry.configureAll() }


@Raptor.Dsl3
fun RaptorComponentScope<KtorRouteRaptorComponent>.newGraph(
	vararg tags: Any = emptyArray(),
	configure: RaptorComponentScope<GraphRaptorComponent>.() -> Unit = {}
) {
	install(GraphKtorFeature)

	raptorComponentSelection {
		registry.register(
			component = GraphRaptorComponent(raptorTags = tags.toHashSet()),
			configure = configure
		)
	}
}
