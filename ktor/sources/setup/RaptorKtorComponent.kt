package io.fluidsonic.raptor


class RaptorKtorComponent internal constructor(
	parent: RaptorFeatureSetup
) : RaptorComponent by parent {

	internal val serverComponents = mutableListOf<RaptorKtorServerComponent>()


	internal fun complete() = RaptorKtorConfig(
		servers = serverComponents.map { it.complete() }
	)
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorComponent>.newServer(
	vararg tags: Any = emptyArray(),
	config: RaptorConfigurable<RaptorKtorServerComponent>.() -> Unit
) {
	TODO()
	//servers.create(tags = *tags, config = config)

	//fun RaptorSetupComponentCollection<RaptorKtorServerComponent>.create(vararg tags: Any = emptyArray(), config: RaptorKtorServerComponent.() -> Unit)

//
//	override fun RaptorSetupComponentCollection<RaptorKtorServerComponent>.create(vararg tags: Any, config: RaptorKtorServerComponent.() -> Unit) {
//		val setup = RaptorKtorServerSetupImpl(parent = this@RaptorKtorComponent)
//
//		serverComponents += setup
//		raptorSetupContext.register<RaptorKtorServerComponent>(setup = setup, tags = *tags, config = config)
//	}
}


@Raptor.Dsl3
val RaptorConfigurable<RaptorKtorComponent>.servers
	get() = raptorSetupContext.getOrCreateComponentCollection<RaptorKtorServerComponent>()
