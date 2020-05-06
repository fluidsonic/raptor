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


// FIXME won't work as it won't go into child registries
@Raptor.Dsl3
val RaptorFeatureComponent.graphs: RaptorComponentConfig<GraphRaptorComponent>
	get() = RaptorComponentConfig.new { configure ->
		ktor {
			servers {
				routes(recursive = true) {
					componentRegistry.configureAll(configure)
				}
			}
		}
	}


@Raptor.Dsl3
fun KtorRouteRaptorComponent.newGraph(
	vararg tags: Any = emptyArray(),
	configure: GraphRaptorComponent.() -> Unit = {}
) {
	install(GraphKtorFeature)

	componentRegistry.register(
		component = GraphRaptorComponent(raptorTags = tags.toHashSet()),
		configure = configure
	)
}
