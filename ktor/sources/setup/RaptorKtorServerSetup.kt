package io.fluidsonic.raptor

import io.ktor.application.*


interface RaptorKtorServerSetup : RaptorSetupElement, RaptorSetupScope.KodeinScope {

	val routes: RaptorSetupComponentCollection<RaptorKtorRouteSetup>

	fun configureKtorApplication(config: Application.() -> Unit)
	fun install(feature: RaptorKtorServerFeature)

	fun RaptorSetupComponentCollection<RaptorKtorRouteSetup>.create(path: String, vararg tags: Any = emptyArray(), config: RaptorKtorRouteSetup.() -> Unit)
}


fun RaptorKtorServerSetup.route(vararg tags: Any = emptyArray(), config: RaptorKtorRouteSetup.() -> Unit) {
	routes.create(path = "", tags = *tags, config = config)
}


fun RaptorKtorServerSetup.route(path: String, vararg tags: Any = emptyArray(), config: RaptorKtorRouteSetup.() -> Unit) {
	routes.create(path = path, tags = *tags, config = config)
}
