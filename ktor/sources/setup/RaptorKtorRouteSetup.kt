package io.fluidsonic.raptor

import io.ktor.routing.*


interface RaptorKtorRouteSetup : RaptorSetupElement, RaptorSetupScope.KodeinScope {

	val routes: RaptorSetupComponentCollection<RaptorKtorRouteSetup>

	fun configureKtorRoute(config: Route.() -> Unit)
	fun install(feature: RaptorKtorRouteFeature)

	fun RaptorSetupComponentCollection<RaptorKtorRouteSetup>.create(path: String, vararg tags: Any = emptyArray(), config: RaptorKtorRouteSetup.() -> Unit)
}


fun RaptorKtorRouteSetup.route(path: String, vararg tags: Any = emptyArray(), config: RaptorKtorRouteSetup.() -> Unit) {
	routes.create(path = path, tags = *tags, config = config)
}
