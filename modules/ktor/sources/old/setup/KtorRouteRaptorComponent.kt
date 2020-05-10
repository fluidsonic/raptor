package io.fluidsonic.raptor

import io.fluidsonic.raptor.internal.KtorRouteConfiguration
import io.ktor.routing.*
import org.kodein.di.*


class KtorRouteRaptorComponent internal constructor(
	private val globalComponent: RaptorFeatureSetup,
	parentComponentRegistry: RaptorComponentRegistry.Mutable,
	private val path: String,
	override val raptorTags: Set<Any>,
	private val serverComponent: KtorServerRaptorComponent
) : RaptorComponent.Default<KtorRouteRaptorComponent>(), RaptorComponent.KodeinBoundary, RaptorComponent.Taggable {

	private val customConfigs = mutableListOf<Route.() -> Unit>()
	private val features = mutableSetOf<KtorRouteFeature>()
	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private val routeComponents = mutableListOf<KtorRouteRaptorComponent>()
	private var wrapper: (Route.(next: Route.() -> Unit) -> Unit)? = null

	val componentRegistry = parentComponentRegistry.createChild()


	internal fun complete(serverCompletion: KtorServerFeatureSetupCompletion): KtorRouteConfiguration {
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
			routeComponent.complete(serverCompletion = serverCompletion)
		}

		return KtorRouteConfiguration(
			children = children,
			customConfig = customConfig,
			kodeinModule = kodeinModule,
			path = path,
			wrapper = wrapper
		)
	}


	@Raptor.Dsl3
	fun custom(configure: RaptorKtorRoute.() -> Unit) {
		customConfigs += configure
	}


	@Raptor.Dsl3
	fun install(feature: KtorRouteFeature) {
		if (!features.add(feature))
			return

		val featureComponent = KtorRouteFeatureComponent(
			globalComponent = globalComponent,
			routeComponent = this@KtorRouteRaptorComponent,
			serverComponent = serverComponent
		)

		componentRegistry.register(featureComponent)

		with(feature) {
			featureComponent.setup()
		}
	}


	override fun kodein(configure: Kodein.Builder.() -> Unit) {
		kodeinConfigs += configure
	}


	@Raptor.Dsl3
	fun newRoute(
		path: String,
		vararg tags: Any = emptyArray(),
		configure: KtorRouteRaptorComponent.() -> Unit
	) {
		val routeComponent = KtorRouteRaptorComponent(
			globalComponent = globalComponent,
			parentComponentRegistry = componentRegistry,
			path = path,
			raptorTags = tags.toHashSet(),
			serverComponent = serverComponent
		)
		routeComponents += routeComponent

		componentRegistry.register(routeComponent, configure = configure)
	}


	// FIXME recursive vs non-recursive/scoped
	@Raptor.Dsl3
	val routes: RaptorComponentSet<KtorRouteRaptorComponent> = componentRegistry.configureAll()


	@Raptor.Dsl3
	fun routes(recursive: Boolean): RaptorComponentSet<KtorRouteRaptorComponent> =
		when (recursive) {
			true -> RaptorComponentSet.new { configure ->
				routes {
					configure()
					configureRecursively(configure)
				}
			}
			false -> routes
		}


	@Raptor.Dsl3
	fun routes(recursive: Boolean, configure: KtorRouteRaptorComponent.() -> Unit) =
		routes(recursive).invoke(configure)


	@Raptor.Dsl3
	fun wrap(wrapper: Route.(next: Route.() -> Unit) -> Unit) {
		val previousWrapper = this.wrapper
		if (previousWrapper != null)
			this.wrapper = { next ->
				previousWrapper { wrapper(next) }
			}
		else
			this.wrapper = wrapper
	}


	// FIXME cleanup

	internal fun configureRecursively(configure: KtorRouteRaptorComponent.() -> Unit) {
		routes {
			configure()
			configureRecursively(configure)
		}
	}


	internal object Key : RaptorComponentKey<KtorRouteRaptorComponent> {

		override fun toString() = "ktor route"
	}
}
