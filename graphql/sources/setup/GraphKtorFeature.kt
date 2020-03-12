package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.routing.*
import org.kodein.di.erased.*


// FIXME Also, how to "complete" a configuration?
// FIXME does this have to be a feature?
object GraphKtorFeature : KtorRouteFeature {

	override fun RaptorFeatureSetup.setup(target: RaptorConfigurable<KtorRouteRaptorComponent>) {
		// FIXME scope to local and add global as option?!

		target {
			custom {
				get {
					dkodein.instance<GraphRoute>().handle(call)
				}

				post {
					dkodein.instance<GraphRoute>().handle(call)
				}
			}
		}
	}


	override fun RaptorFeatureSetupCompletion.completeSetup() {
		// FIXME scope to local and add global as option?!

		val definitions = component<GraphRaptorComponent>()
			?.definitions
			?.toList()
			?: return

		kodein {
			bind() from singleton {
				GraphRoute(
					system = GraphSystem(definitions = definitions)
				)
			}
		}
	}
}


@Raptor.Dsl3
val RaptorConfigurable<RaptorFeatureComponent>.graphs: RaptorConfigurableCollection<GraphRaptorComponent>
	get() = raptorComponentRegistry.configureAll()


@Raptor.Dsl3
fun RaptorConfigurable<KtorRouteRaptorComponent>.newGraph(
	vararg tags: Any = emptyArray(),
	configure: RaptorConfigurable<GraphRaptorComponent>.() -> Unit = {}
) {
	install(GraphKtorFeature)

	// FIXME fail on duplicate
	raptorComponentRegistry.register(
		component = GraphRaptorComponent(raptorTags = tags.toHashSet()),
		configure = configure
	)
}
