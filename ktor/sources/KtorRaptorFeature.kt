package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*


object KtorRaptorFeature : RaptorFeature {

	override fun RaptorFeatureCompletion.complete() {
		raptorSetupContext.configure<RaptorKtorSetupImpl> { // FIXME hack
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
fun RaptorFeatureSetup.ktor(config: RaptorKtorSetup.() -> Unit) {
	val setup = RaptorKtorSetupImpl(parent = this)

	raptorSetupContext.register<RaptorKtorSetup>(setup = setup, config = config)
	raptorSetupContext.register(setup = setup, config = config)
}
