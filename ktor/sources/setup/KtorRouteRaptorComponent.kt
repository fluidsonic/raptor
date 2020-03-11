package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


class KtorRouteRaptorComponent internal constructor(
	internal val featureComponent: RaptorFeatureComponent,
	private val path: String,
	override val raptorTags: Set<Any>
) : RaptorComponent.Taggable {

	internal val customConfigs = mutableListOf<Route.() -> Unit>()
	internal val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	internal val routeComponents = mutableListOf<KtorRouteRaptorComponent>()


	internal fun complete(): KtorRouteConfig {
		// FIXME check/clean path

		val kodeinModule = Kodein.Module(name = "raptor/server/route[path=$path]") { // FIXME server id
			for (config in kodeinConfigs)
				config()
		}

		val customConfig = customConfigs.flatten()
		val children = routeComponents.map { it.complete() }

		return KtorRouteConfig(
			children = children,
			customConfig = customConfig,
			kodeinModule = kodeinModule,
			path = path
		)
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorRouteRaptorComponent>.custom(configure: Route.() -> Unit) {
	raptorComponentConfiguration {
		customConfigs += configure
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorRouteRaptorComponent>.install(feature: KtorRouteFeature) {
	val target = this

	raptorComponentConfiguration {
		with(feature) {
			featureComponent.setup(target = target)
		}
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorRouteRaptorComponent>.kodein(configure: Kodein.Builder.() -> Unit) { // FIXME make own API
	raptorComponentConfiguration {
		kodeinConfigs += configure
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<KtorRouteRaptorComponent>.newRoute(
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
val RaptorConfigurable<KtorRouteRaptorComponent>.routes: RaptorConfigurableCollection<KtorRouteRaptorComponent>
	get() = raptorComponentRegistry.configureAll()


@Raptor.Dsl3
fun RaptorConfigurable<KtorRouteRaptorComponent>.wrap(wrapper: Route.(next: Route.() -> Unit) -> Route) {
	TODO()
}
