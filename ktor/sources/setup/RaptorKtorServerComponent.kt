package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


class RaptorKtorServerComponent internal constructor(
	parent: RaptorKtorComponent
) : RaptorComponent by parent {

	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private val ktorApplicationConfigs = mutableListOf<Application.() -> Unit>()
	private val routeSetups = mutableListOf<RaptorKtorRouteSetupImpl>()


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


	// FIXME duplicates
	override fun install(feature: RaptorKtorServerFeature) {
		with(feature) {
			raptorSetupContext.configure<RaptorFeatureSetup> {
				setup(scope = this@RaptorKtorServerComponent)
			}
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}


	// FIXME generalize so that code between server routes and child routes can be reused
	override fun RaptorComponentCollection<RaptorKtorRouteComponent>.create(
		path: String,
		vararg tags: Any,
		config: RaptorKtorRouteComponent.() -> Unit
	) {
		val setup = RaptorKtorRouteSetupImpl(
			path = path,
			parent = this@RaptorKtorServerComponent
		)

		routeSetups += setup
		raptorSetupContext.register<RaptorKtorRouteComponent>(setup = setup, tags = *tags, config = config)
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorServerComponent>.custom(config: Application.() -> Unit) {
	TODO()
	//ktorApplicationConfigs += config
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorServerComponent>.kodein(config: Kodein.Builder.() -> Unit) { // FIXME make own API
	forEachComponent {
		TODO()
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorServerComponent>.newRoute(
	vararg tags: Any = emptyArray(),
	config: RaptorConfigurable<RaptorKtorRouteComponent>.() -> Unit
) {
	routes.create(path = "", tags = *tags, config = config)
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorServerComponent>.newRoute(
	path: String,
	vararg tags: Any = emptyArray(),
	config: RaptorConfigurable<RaptorKtorRouteComponent>.() -> Unit
) {
	routes.create(path = path, tags = *tags, config = config)
}


@Raptor.Dsl3
val RaptorConfigurable<RaptorKtorServerComponent>.routes // FIXME recursive vs non-recursive
	get(): RaptorConfigurableCollection<RaptorKtorRouteComponent> {
		return raptorSetupContext.getComponentCollection<RaptorKtorRouteComponent>()
	}
