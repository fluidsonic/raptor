package io.fluidsonic.raptor

import org.kodein.di.erased.*


@Raptor.Dsl3
object KtorRaptorFeature : RaptorFeature {

	override fun RaptorFeatureSetup.setup() {
		raptorComponentRegistry.register(KtorRaptorComponent(
			featureSetup = this
		))
	}


	override fun RaptorFeatureSetupCompletion.completeSetup() {
		val config = component<KtorRaptorComponent>()?.complete() ?: return

		if (config.servers.isNotEmpty()) {
			for (serverConfig in config.servers)
				kodein {
					bind<KtorServerImpl>(tag = serverConfig) with singleton {
						KtorServerImpl(
							config = serverConfig,
							parentContext = instance()
						)
					}
				}

			onStart {
				for (server in allInstances<KtorServerImpl>())
					server.start()
			}

			onStop {
				for (server in allInstances<KtorServerImpl>())
					server.stop()
			}
		}
	}
}


@Raptor.Dsl3
val RaptorConfigurable<RaptorFeatureComponent>.ktor: RaptorConfigurable<KtorRaptorComponent>
	get() {
		install(KtorRaptorFeature) // FIXME check duplicates

		return raptorComponentRegistry.configureSingle()
	}
