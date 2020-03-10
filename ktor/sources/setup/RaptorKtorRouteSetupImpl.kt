package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*
import io.ktor.routing.*
import org.kodein.di.*


internal class RaptorKtorRouteSetupImpl(
	private val path: String,
	parent: RaptorSetupElement
) : RaptorKtorRouteSetup, RaptorSetupElement by parent, RaptorSetupScope.KodeinScope {

	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private val ktorRouteConfigs = mutableListOf<Route.() -> Unit>()
	private val routeSetups = mutableListOf<RaptorKtorRouteSetupImpl>()

	override val routes = raptorSetupContext.collection<RaptorKtorRouteSetupImpl>()


	fun complete(): RaptorKtorRouteConfig {
		// FIXME check/clean path

		val kodeinModule = Kodein.Module(name = "raptor/server/route[path=$path]") { // FIXME server id
			for (config in kodeinConfigs)
				config()
		}

		val ktorRouteConfig = ktorRouteConfigs.flatten()
		val children = routeSetups.map { it.complete() }

		return RaptorKtorRouteConfig(
			children = children,
			kodeinModule = kodeinModule,
			ktorConfig = ktorRouteConfig,
			path = path
		)
	}


	override fun configureKtorRoute(config: Route.() -> Unit) {
		ktorRouteConfigs += config
	}


	// FIXME duplicates
	override fun install(feature: RaptorKtorRouteFeature) {
		with(feature) {
			raptorSetupContext.configure<RaptorFeatureSetup> {
				setup(scope = this@RaptorKtorRouteSetupImpl)
			}
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}


	fun route(path: String, vararg tags: Any = emptyArray(), config: RaptorKtorRouteSetup.() -> Unit) {
		routes.create(path = path, tags = *tags, config = config)
	}


	// FIXME generalize so that code between server routes and child routes can be reused
	override fun RaptorSetupComponentCollection<RaptorKtorRouteSetup>.create(path: String, vararg tags: Any, config: RaptorKtorRouteSetup.() -> Unit) {
		val setup = RaptorKtorRouteSetupImpl(
			path = path, // FIXME subpath
			parent = this@RaptorKtorRouteSetupImpl
		)

		routeSetups += setup
		raptorSetupContext.register<RaptorKtorRouteSetup>(setup = setup, tags = *tags, config = config)
	}
}
