package io.fluidsonic.raptor


@Raptor.Dsl3
object KtorRaptorFeature : RaptorFeature {

	override fun RaptorFeatureComponent.setup() {
		raptorComponentRegistry.register(KtorRaptorComponent(
			featureComponent = this
		))
	}


	override fun RaptorFeatureSetupCompletion.completeSetup() {
		val config = component<KtorRaptorComponent>()?.complete() ?: return

		val servers = config.servers.map(::KtorServer)
		if (servers.isNotEmpty()) {
			onStart {
				for (server in servers)
					server.start()
			}

			onStop {
				for (server in servers)
					server.stop()
			}
		}
	}
}


@Raptor.Dsl3
val RaptorFeatureComponent.ktor: RaptorConfigurable<KtorRaptorComponent>
	get() {
		install(KtorRaptorFeature) // FIXME check duplicates

		return raptorComponentRegistry.configureSingle()
	}
