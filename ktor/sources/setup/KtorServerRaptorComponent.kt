package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


class KtorServerRaptorComponent internal constructor(
	internal val featureComponent: RaptorFeatureComponent,
	override val raptorTags: Set<Any>
) : RaptorComponent.Taggable {

	internal val customConfigs = mutableListOf<Application.() -> Unit>()
	internal val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	internal val routeComponents = mutableListOf<KtorRouteRaptorComponent>()


	internal fun complete(): KtorServerConfig {
		val kodeinModule = Kodein.Module(name = "raptor/server") { // FIXME server id
			for (config in kodeinConfigs)
				config()
		}

		val customConfig = customConfigs.flatten()

		val routingConfig = routeComponents
			.map { it.complete() }
			.ifEmpty { null }
			?.let {
				KtorRouteConfig(
					children = it,
					kodeinModule = kodeinModule,
					customConfig = {},
					path = ""
				)
			}

		return KtorServerConfig(
			customConfig = customConfig,
			kodeinModule = kodeinModule,
			routingConfig = routingConfig
		)
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorServerRaptorComponent>.custom(config: Application.() -> Unit) {
	raptorComponentConfiguration {
		customConfigs += config
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorServerRaptorComponent>.install(feature: KtorServerFeature) {
	val target = this

	raptorComponentConfiguration {
		with(feature) {
			featureComponent.setup(target = target)
		}
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorServerRaptorComponent>.kodein(configure: Kodein.Builder.() -> Unit) { // FIXME make own API
	raptorComponentConfiguration {
		kodeinConfigs += configure
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorServerRaptorComponent>.newRoute(
	vararg tags: Any = emptyArray(),
	configure: RaptorConfigurable<KtorRouteRaptorComponent>.() -> Unit
) {
	newRoute(path = "", tags = *tags, configure = configure)
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorServerRaptorComponent>.newRoute(
	path: String,
	vararg tags: Any = emptyArray(),
	configure: RaptorConfigurable<KtorRouteRaptorComponent>.() -> Unit
) {
	val registry = raptorComponentRegistry

	raptorComponentConfiguration {
		val component = KtorRouteRaptorComponent(
			featureComponent = featureComponent,
			path = path,
			raptorTags = tags.toSet()
		)
		routeComponents += component

		registry.register(component, configure = configure)
	}
}


@Raptor.Dsl3
val RaptorConfigurable<KtorServerRaptorComponent>.routes: RaptorConfigurableCollection<KtorRouteRaptorComponent> // FIXME recursive vs non-recursive/scoped
	get() = raptorComponentRegistry.configureAll()
