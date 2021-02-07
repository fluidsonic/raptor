package io.fluidsonic.raptor

import io.ktor.routing.*


public class KtorRouteRaptorComponent internal constructor(
	internal val globalScope: RaptorTopLevelConfigurationScope,
	private val host: String?,
	private val path: String,
	internal val serverComponentRegistry: RaptorComponentRegistry,
) : RaptorComponent.Default<KtorRouteRaptorComponent>(),
	RaptorTaggableComponent,
	RaptorTransactionGeneratingComponent {

	// FIXME ok not to specify parent?
	private val propertyRegistry = RaptorPropertyRegistry.default()

	internal val customConfigurations = mutableListOf<Route.() -> Unit>()
	internal val features = mutableSetOf<KtorRouteFeature>()
	internal var wrapper: (Route.(next: Route.() -> Unit) -> Unit)? = null

	internal lateinit var scopes: Scopes


	internal fun RaptorComponentConfigurationEndScope.toRouteConfigurations(): KtorRouteConfiguration {
		// FIXME check/clean path

		val children = componentRegistry.many(Key).map { routeComponent ->
			with(routeComponent) {
				toRouteConfigurations()
			}
		}

		return KtorRouteConfiguration(
			children = children,
			customConfigurations = customConfigurations.toList(),
			host = host,
			path = path,
			properties = this@KtorRouteRaptorComponent.propertyRegistry.toSet(),
			transactionFactory = transactionFactory(this@KtorRouteRaptorComponent),
			wrapper = wrapper
		)
	}


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		for (feature in features)
			with(feature) {
				scopes.onConfigurationEnded()
			}
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		scopes = Scopes(
			globalScope = globalScope,
			propertyRegistry = propertyRegistry,
			route = this@KtorRouteRaptorComponent,
			serverComponentRegistry = serverComponentRegistry
		)
	}


	internal object Key : RaptorComponentKey<KtorRouteRaptorComponent> {

		override fun toString() = "ktor route"
	}


	internal class Scopes(
		private val globalScope: RaptorTopLevelConfigurationScope,
		propertyRegistry: RaptorPropertyRegistry,
		override val route: KtorRouteRaptorComponent,
		serverComponentRegistry: RaptorComponentRegistry,
	) : KtorRouteFeatureConfigurationEndScope,
		KtorRouteFeatureConfigurationStartScope {

		private val routeScope = RouteScope(componentRegistry = route.componentRegistry, propertyRegistry = propertyRegistry)
		private val serverScope = ServerScope(componentRegistry = serverComponentRegistry, propertyRegistry = propertyRegistry)


		override fun global(configuration: RaptorTopLevelConfigurationScope.() -> Unit) {
			globalScope.configuration()
		}


		override fun route(configuration: KtorRouteFeatureConfigurationEndScope.RouteScope.() -> Unit) {
			routeScope.configuration()
		}


		override fun server(configuration: KtorRouteFeatureConfigurationEndScope.ServerScope.() -> Unit) {
			serverScope.configuration()
		}


		private class RouteScope(
			override val componentRegistry: RaptorComponentRegistry,
			override val propertyRegistry: RaptorPropertyRegistry,
		) : KtorRouteFeatureConfigurationEndScope.RouteScope


		private class ServerScope(
			override val componentRegistry: RaptorComponentRegistry,
			override val propertyRegistry: RaptorPropertyRegistry,
		) : KtorRouteFeatureConfigurationEndScope.ServerScope
	}
}


@RaptorDsl
public fun RaptorComponentSet<KtorRouteRaptorComponent>.custom(configure: RaptorKtorRouteInitializationScope.() -> Unit) {
	configure {
		customConfigurations += configure
	}
}


@RaptorDsl
public fun RaptorComponentSet<KtorRouteRaptorComponent>.install(feature: KtorRouteFeature) {
	configure {
		if (features.add(feature))
			with(feature) {
				scopes.onConfigurationStarted()
			}
	}
}


@RaptorDsl
public fun RaptorComponentSet<KtorRouteRaptorComponent>.newRoute(path: String): RaptorComponentSet<KtorRouteRaptorComponent> =
	withComponentAuthoring {
		map {
			KtorRouteRaptorComponent(
				globalScope = globalScope,
				host = null,
				path = path,
				serverComponentRegistry = serverComponentRegistry
			)
				.also { componentRegistry.register(KtorRouteRaptorComponent.Key, it) }
		}
	}


@RaptorDsl
public fun RaptorComponentSet<KtorRouteRaptorComponent>.newRoute(path: String, configure: KtorRouteRaptorComponent.() -> Unit) {
	newRoute(path).configure(configure)
}


@RaptorDsl
public val RaptorComponentSet<KtorRouteRaptorComponent>.routes: RaptorComponentSet<KtorRouteRaptorComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.configure(KtorRouteRaptorComponent.Key)
		}
	}


@RaptorDsl
public fun RaptorComponentSet<KtorRouteRaptorComponent>.routes(recursive: Boolean): RaptorComponentSet<KtorRouteRaptorComponent> =
	withComponentAuthoring {
		when (recursive) {
			true -> componentSet { action ->
				authoredSet.routes {
					action()
					routes(recursive = true).configure(action)
				}
			}
			false -> authoredSet.routes
		}
	}


@RaptorDsl
public fun RaptorComponentSet<KtorRouteRaptorComponent>.routes(recursive: Boolean, configure: KtorRouteRaptorComponent.() -> Unit) {
	routes(recursive = recursive).invoke(configure)
}


@RaptorDsl
public fun RaptorComponentSet<KtorRouteRaptorComponent>.wrap(wrapper: RaptorKtorRouteInitializationScope.(next: Route.() -> Unit) -> Unit) {
	configure {
		val previousWrapper = this.wrapper
		if (previousWrapper != null)
			this.wrapper = { next ->
				previousWrapper { wrapper(next) }
			}
		else
			this.wrapper = wrapper
	}
}
