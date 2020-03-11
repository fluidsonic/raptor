package io.fluidsonic.raptor


@Raptor.Dsl3
object KtorRaptorFeature : RaptorFeature {

	override fun RaptorFeatureSetupCompletion.completeSetup() {
		raptorSetupContext.configure<RaptorKtorComponent> { // FIXME hack
			val config = complete()
			val servers = config.servers.map(::RaptorKtorServerImpl)
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
}


// FIXME de-dup
@Raptor.Dsl3
fun RaptorFeatureSetup.ktor(config: RaptorConfigurable<RaptorKtorComponent>.() -> Unit) {
	val setup = RaptorKtorComponent(parent = this)

	raptorSetupContext.register<RaptorKtorComponent>(setup = setup, config = config)
	raptorSetupContext.register(setup = setup, config = config)
}
