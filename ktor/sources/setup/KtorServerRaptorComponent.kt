package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


class KtorServerRaptorComponent internal constructor(
	internal val globalFeatureSetup: RaptorFeatureSetup,
	override val raptorTags: Set<Any>
) : RaptorComponent.Taggable, RaptorComponent.TransactionBoundary<KtorServerTransaction> {

	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()

	internal val customConfigs = mutableListOf<Application.() -> Unit>()
	internal val features = mutableListOf<KtorServerFeature>()
	internal val routeComponents = mutableListOf<KtorRouteRaptorComponent>()


	internal fun complete(globalCompletion: RaptorFeatureSetupCompletion): KtorServerConfig {
		val completion = KtorServerFeatureSetupCompletion(
			componentRegistry = globalCompletion.componentRegistry.getSingle(this).registry,
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


	override fun kodein(configure: Kodein.Builder.() -> Unit) {
		kodeinConfigs += configure
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorServerRaptorComponent>.custom(config: RaptorKtorApplication.() -> Unit) {
	raptorComponentSelection {
		component.customConfigs += config
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorServerRaptorComponent>.install(feature: KtorServerFeature) {
	raptorComponentSelection {
		if (component.features.add(feature))
			with(feature) {
				registry.configureSingleOrCreate {
					KtorServerFeatureComponent(
						globalFeatureSetup = component.globalFeatureSetup,
						serverComponent = component
					)
				}.setup()
			}
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorServerRaptorComponent>.newRoute(
	vararg tags: Any = emptyArray(),
	configure: RaptorComponentScope<KtorRouteRaptorComponent>.() -> Unit
) {
	newRoute(path = "", tags = *tags, configure = configure)
}


@Raptor.Dsl3
fun RaptorComponentScope<KtorServerRaptorComponent>.newRoute(
	path: String,
	vararg tags: Any = emptyArray(),
	configure: RaptorComponentScope<KtorRouteRaptorComponent>.() -> Unit
) {
	raptorComponentSelection {
		val routeComponent = KtorRouteRaptorComponent(
			globalFeatureSetup = component.globalFeatureSetup,
			path = path,
			raptorTags = tags.toHashSet(),
			serverComponent = component
		)
		component.routeComponents += routeComponent

		registry.register(routeComponent, configure = configure, definesScope = true)
	}
}


@Raptor.Dsl3
val RaptorComponentScope<KtorServerRaptorComponent>.routes: RaptorComponentScope.Collection<KtorRouteRaptorComponent> // FIXME recursive vs non-recursive/scoped
	get() = raptorComponentSelection.map { registry.configureAll() }
