package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*


class RaptorKtorSetupImpl internal constructor(
	parent: RaptorFeatureSetup
) : RaptorKtorSetup, RaptorSetupElement by parent {

	private val serverSetups = mutableListOf<RaptorKtorServerSetupImpl>()

	override val servers = raptorSetupContext.collection<RaptorKtorServerSetup>()


	internal fun complete() = RaptorKtorConfig(
		servers = serverSetups.map { it.complete() }
	)


	fun server(vararg tags: Any = emptyArray(), config: RaptorKtorServerSetup.() -> Unit) {
		servers.create(tags = *tags, config = config)
	}


	override fun RaptorSetupComponentCollection<RaptorKtorServerSetup>.create(vararg tags: Any, config: RaptorKtorServerSetup.() -> Unit) {
		val setup = RaptorKtorServerSetupImpl(parent = this@RaptorKtorSetupImpl)

		serverSetups += setup
		raptorSetupContext.register<RaptorKtorServerSetup>(setup = setup, tags = *tags, config = config)
	}
}
