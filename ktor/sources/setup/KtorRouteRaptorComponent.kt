package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


class KtorRouteRaptorComponent internal constructor(
	internal val globalFeatureSetup: RaptorFeatureSetup,
	private val path: String,
	override val raptorTags: Set<Any>,
	internal val serverComponent: KtorServerRaptorComponent
) : RaptorComponent.KodeinBoundary, RaptorComponent.Taggable {

	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()

	internal val customConfigs = mutableListOf<Route.() -> Unit>()
	internal val features = mutableSetOf<KtorRouteFeature>()
	internal val routeComponents = mutableListOf<KtorRouteRaptorComponent>()
	internal var wrapper: (Route.(next: Route.() -> Unit) -> Route)? = null


	internal fun complete(
		componentRegistry: RaptorComponentRegistry,
		serverCompletion: KtorServerFeatureSetupCompletion
	): KtorRouteConfig {
		// FIXME check/clean path

		val completion = KtorRouteFeatureSetupCompletion(
			componentRegistry = componentRegistry,
			serverCompletion = serverCompletion
		)

		for (feature in features)
			with(feature) {
				completion.completeSetup()
			}

		val kodeinModule = Kodein.Module(name = "raptor/server/route[path=$path]") { // FIXME server id
			for (config in kodeinConfigs)
				config()

			for (config in completion.kodeinConfigs)
				config()
		}

		val customConfig = customConfigs.flatten()
		val children = routeComponents.map { routeComponent ->
			routeComponent.complete(
				componentRegistry = componentRegistry.getSingle(routeComponent).registry,
				serverCompletion = serverCompletion
			)
		}

		return KtorRouteConfig(
			children = children,
			customConfig = customConfig,
			kodeinModule = kodeinModule,
			path = path,
			wrapper = wrapper
		)
	}


	override fun kodein(configure: Kodein.Builder.() -> Unit) {
		kodeinConfigs += configure
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorRouteRaptorComponent>.custom(configure: RaptorKtorRoute.() -> Unit) {
	raptorComponentSelection {
		component.customConfigs += configure
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorRouteRaptorComponent>.install(feature: KtorRouteFeature) {
	raptorComponentSelection {
		if (component.features.add(feature))
			with(feature) {
				registry.configureSingleOrCreate {
					KtorRouteFeatureComponent(
						globalFeatureSetup = component.globalFeatureSetup,
						routeComponent = component,
						serverComponent = component.serverComponent
					)
				}.setup()
			}
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorRouteRaptorComponent>.newRoute(
	path: String,
	vararg tags: Any = emptyArray(),
	configure: RaptorComponentScope<KtorRouteRaptorComponent>.() -> Unit
) {
	raptorComponentSelection {
		val routeComponent = KtorRouteRaptorComponent(
			globalFeatureSetup = component.globalFeatureSetup,
			path = path,
			raptorTags = tags.toHashSet(),
			serverComponent = component.serverComponent
		)
		component.routeComponents += routeComponent

		registry.register(routeComponent, configure = configure, definesScope = true)
	}
}


@Raptor.Dsl3
val RaptorComponentScope<KtorRouteRaptorComponent>.routes: RaptorComponentScope.Collection<KtorRouteRaptorComponent>
	get() = raptorComponentSelection.map { registry.configureAll() }


@Raptor.Dsl3
fun RaptorComponentScope<KtorRouteRaptorComponent>.wrap(wrapper: Route.(next: Route.() -> Unit) -> Route) {
	raptorComponentSelection {
		val previousWrapper = component.wrapper
		if (previousWrapper != null)
			component.wrapper = { next ->
				previousWrapper { wrapper(next) }
			}
		else
			component.wrapper = wrapper
	}
}
