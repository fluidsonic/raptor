package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
class KtorServerFeatureSetupCompletion internal constructor(
	componentRegistry: RaptorComponentRegistry,
	internal val globalCompletion: RaptorFeatureSetupCompletion
) {

	internal val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()
	internal val server = Server(componentRegistry = componentRegistry)


	@Raptor.Dsl3
	fun global(configure: RaptorFeatureSetupCompletion.() -> Unit) {
		globalCompletion.apply(configure)
	}


	@Raptor.Dsl3
	fun server(configure: Server.() -> Unit) {
		server.apply(configure)
	}


	inner class Server internal constructor(
		val componentRegistry: RaptorComponentRegistry
	) {

		@Raptor.Dsl3
		fun kodein(config: Kodein.Builder.() -> Unit) {
			kodeinConfigs += config
		}
	}
}
