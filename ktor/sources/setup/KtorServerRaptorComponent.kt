package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


class KtorServerRaptorComponent internal constructor(
	private val globalComponent: RaptorFeatureComponent,
	parentComponentRegistry: RaptorComponentRegistry.Mutable,
	override val raptorTags: Set<Any>
) : RaptorComponent.Taggable, RaptorComponent.TransactionBoundary<KtorServerTransaction> {

	private val customConfigs = mutableListOf<Application.() -> Unit>()
	private val features = mutableListOf<KtorServerFeature>()
	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private val routeComponents = mutableListOf<KtorRouteRaptorComponent>()

	val componentRegistry = parentComponentRegistry.createChild()


	internal fun complete(globalCompletion: RaptorFeatureSetupCompletion): KtorServerConfig {
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

		val kodeinModule = Kodein.Module(name = "raptor/server") { // FIXME server id
			for (config in kodeinConfigs)
				config()

			for (config in completion.kodeinConfigs)
				config()
		}

		val customConfig = customConfigs.flatten()

		val routingConfig = routeConfigs
			.ifEmpty { null }
			?.let {
				KtorRouteConfig(
					children = it,
					kodeinModule = kodeinModule,
					customConfig = {},
					path = "",
					wrapper = null
				)
			}

		return KtorServerConfig(
			customConfig = customConfig,
			kodeinModule = kodeinModule,
			routingConfig = routingConfig
		)
	}


	@Raptor.Dsl3
	fun custom(config: RaptorKtorApplication.() -> Unit) {
		customConfigs += config
	}


	override fun kodein(configure: Kodein.Builder.() -> Unit) {
		kodeinConfigs += configure
	}


	@Raptor.Dsl3
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


	@Raptor.Dsl3
	fun newRoute(
		vararg tags: Any = emptyArray(),
		configure: KtorRouteRaptorComponent.() -> Unit
	) {
		newRoute(path = "", tags = *tags, configure = configure)
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
			serverComponent = this@KtorServerRaptorComponent
		)
		routeComponents += routeComponent

		componentRegistry.register(routeComponent, configure = configure)
	}


	@Raptor.Dsl3
	val routes: RaptorComponentConfig<KtorRouteRaptorComponent> = componentRegistry.configureAll()


	@Raptor.Dsl3
	fun routes(recursive: Boolean): RaptorComponentConfig<KtorRouteRaptorComponent> =
		when (recursive) {
			true -> RaptorComponentConfig.new { configure ->
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


	// FIXME invalid scope
	override val transactions: RaptorComponentConfig<RaptorTransactionComponent> = componentRegistry.configureSingle()
}
