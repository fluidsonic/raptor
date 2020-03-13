package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
class KtorRouteFeatureSetupCompletion internal constructor(
	componentRegistry: RaptorComponentRegistry,
	internal val serverCompletion: KtorServerFeatureSetupCompletion
) {

	private val route = Route(componentRegistry = componentRegistry)

	internal val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()


	@Raptor.Dsl3
	fun global(configure: RaptorFeatureSetupCompletion.() -> Unit) {
		serverCompletion.globalCompletion.apply(configure)
	}


	@Raptor.Dsl3
	fun route(configure: Route.() -> Unit) {
		route.apply(configure)
	}


	@Raptor.Dsl3
	fun server(configure: KtorServerFeatureSetupCompletion.Server.() -> Unit) {
		serverCompletion.server.apply(configure)
	}


	inner class Route internal constructor(
		val componentRegistry: RaptorComponentRegistry
	) {

		@Raptor.Dsl3
		fun kodein(config: Kodein.Builder.() -> Unit) {
			kodeinConfigs += config
		}
	}
}
