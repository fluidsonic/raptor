package io.fluidsonic.raptor


interface RaptorKtorSetup : RaptorSetupElement {

	val servers: RaptorSetupComponentCollection<RaptorKtorServerSetup>

	fun RaptorSetupComponentCollection<RaptorKtorServerSetup>.create(vararg tags: Any = emptyArray(), config: RaptorKtorServerSetup.() -> Unit)
}


fun RaptorKtorSetup.server(vararg tags: Any = emptyArray(), config: RaptorKtorServerSetup.() -> Unit) {
	servers.create(tags = *tags, config = config)
}
