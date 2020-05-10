package io.fluidsonic.raptor

import io.ktor.application.*


// FIXME taggable
class KtorServerRaptorComponent internal constructor() : RaptorComponent.Default<KtorServerRaptorComponent>(), RaptorTransactionGeneratingComponent {

	internal val customConfigurations = mutableListOf<Application.() -> Unit>()
	private val features = mutableListOf<KtorServerFeature>()


	internal fun finalize(): KtorServerConfiguration {
		val completion = KtorServerFeatureSetupCompletion(
			componentRegistry = componentRegistry,
			globalCompletion = globalCompletion
		)

		for (feature in features)
			with(feature) {
				completion.completeSetup()
			}

		val routeConfigs = routeComponents.map { routeComponent ->
			routeComponent.complete(serverCompletion = completion)
		}

		val routingConfig = routeConfigs
			.ifEmpty { null }
			?.let {
				KtorRouteConfiguration(
					children = it,
					customConfig = {},
					path = "",
					wrapper = null
				)
			}

		return KtorServerConfiguration(
			customConfigurations = customConfigurations.toList(),
			routingConfiguration = routingConfig
		)
	}


	@RaptorDsl
	fun install(feature: KtorServerFeature) {
		if (features.add(feature))
			with(feature) {
				componentRegistry.configureSingleOrCreate {
					KtorServerFeatureComponent(
						globalComponent = globalComponent,
						serverComponent = this@KtorServerRaptorComponent
					)
				}.invoke {
					setup()
				}
			}
	}


	internal object Key : RaptorComponentKey<KtorServerRaptorComponent> {

		override fun toString() = "ktor server"
	}
}


@RaptorDsl
fun RaptorComponentSet<KtorServerRaptorComponent>.custom(configuration: RaptorKtorConfigurationScope.() -> Unit) {
	configure {
		customConfigurations += configuration
	}
}


@RaptorDsl
fun RaptorComponentSet<KtorServerRaptorComponent>.newRoute(path: String = "", configure: KtorRouteRaptorComponent.() -> Unit) {
	configure {
		KtorRouteRaptorComponent(path = path)
			.also { componentRegistry.register(KtorRouteRaptorComponent.Key, it) }
			.also(configure)
	}
}


@RaptorDsl
val RaptorComponentSet<KtorServerRaptorComponent>.routes: RaptorComponentSet<KtorRouteRaptorComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.configure(KtorRouteRaptorComponent.Key)
		}
	}


@RaptorDsl
fun RaptorComponentSet<KtorServerRaptorComponent>.routes(recursive: Boolean): RaptorComponentSet<KtorRouteRaptorComponent> =
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
fun RaptorComponentSet<KtorServerRaptorComponent>.routes(recursive: Boolean, configure: KtorRouteRaptorComponent.() -> Unit) =
	routes(recursive).invoke(configure)
