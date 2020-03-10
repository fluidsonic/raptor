package io.fluidsonic.raptor

import io.fluidsonic.raptor.configuration.*
import io.ktor.application.*
import org.kodein.di.*


internal class RaptorKtorServerSetupImpl(
	parent: RaptorKtorSetup
) : RaptorKtorServerSetup, RaptorSetupElement by parent {

	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private val ktorApplicationConfigs = mutableListOf<Application.() -> Unit>()
	private val routeSetups = mutableListOf<RaptorKtorRouteSetupImpl>()

	override val routes = raptorSetupContext.collection<RaptorKtorRouteSetup>()


	fun complete(): RaptorKtorServerConfig {
		val kodeinModule = Kodein.Module(name = "raptor/server") { // FIXME server id
			for (config in kodeinConfigs)
				config()
		}

		val ktorApplicationConfig = ktorApplicationConfigs.flatten()

		val routingConfig = routeSetups
			.map { it.complete() }
			.ifEmpty { null }
			?.let {
				RaptorKtorRouteConfig(
					children = it,
					kodeinModule = kodeinModule,
					ktorConfig = {},
					path = ""
				)
			}

		return RaptorKtorServerConfig(
			kodeinModule = kodeinModule,
			ktorApplicationConfig = ktorApplicationConfig,
			routingConfig = routingConfig
		)
	}


	override fun configureKtorApplication(config: Application.() -> Unit) {
		ktorApplicationConfigs += config
	}


	// FIXME duplicates
	override fun install(feature: RaptorKtorServerFeature) {
		with(feature) {
			raptorSetupContext.configure<RaptorFeatureSetup> {
				setup(scope = this@RaptorKtorServerSetupImpl)
			}
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}


	// FIXME generalize so that code between server routes and child routes can be reused
	override fun RaptorSetupComponentCollection<RaptorKtorRouteSetup>.create(
		path: String,
		vararg tags: Any,
		config: RaptorKtorRouteSetup.() -> Unit
	) {
		val setup = RaptorKtorRouteSetupImpl(
			path = path,
			parent = this@RaptorKtorServerSetupImpl
		)

		routeSetups += setup
		raptorSetupContext.register<RaptorKtorRouteSetup>(setup = setup, tags = *tags, config = config)
	}
}
