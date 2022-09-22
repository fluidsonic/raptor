package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.server.routing.*


// FIXME needs custom property set scope & hierarchy
//FIXME continue here
public class RaptorKtorRouteComponent internal constructor(
	private val host: String?,
	private val path: String,
) : RaptorComponent2.Base(), RaptorTaggableComponent2, RaptorTransactionGeneratingComponent {

	private val customConfigurations = mutableListOf<Route.() -> Unit>()
	private val features = mutableSetOf<RaptorKtorRouteFeature>()
	private var wrapper: (Route.(next: Route.() -> Unit) -> Unit)? = null


	@RaptorDsl
	public fun custom(configure: RaptorKtorRouteInitializationScope.() -> Unit) {
		customConfigurations += configure
	}


	@RaptorDsl
	public fun install(feature: RaptorKtorRouteFeature) {
		if (features.add(feature))
			with(feature) {
				ConfigurationStartScope().onConfigurationStarted()
			}
	}


	@RaptorDsl
	public val routes: RaptorKtorRoutesComponent
		get() = componentRegistry2.oneOrRegister(RaptorKtorRoutesComponent.Key, ::RaptorKtorRoutesComponent)


	@RaptorDsl
	public fun wrap(wrapper: RaptorKtorRouteInitializationScope.(next: Route.() -> Unit) -> Unit) {
		val previousWrapper = this.wrapper
		if (previousWrapper != null)
			this.wrapper = { next ->
				previousWrapper { wrapper(next) }
			}
		else
			this.wrapper = wrapper
	}


	// FIXME rn
	internal fun RaptorComponentConfigurationEndScope2.toRouteConfigurations(): KtorRouteConfiguration {
		// TODO Check/clean paths.

		val routes = componentRegistry2.oneOrNull(RaptorKtorRoutesComponent.Key)
			?.componentRegistry2
			?.many(Key)
			?.map { routeComponent ->
				with(routeComponent) {
					toRouteConfigurations()
				}
			}
			.orEmpty()

		return KtorRouteConfiguration(
			children = routes,
			customConfigurations = customConfigurations.toList(),
			host = host,
			path = path,
			properties = propertyRegistry.toSet(),
			transactionFactory = transactionFactory(this@RaptorKtorRouteComponent),
			wrapper = wrapper,
		)
	}


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		if (features.isEmpty())
			return

		val scope = ConfigurationEndScope(parent = this)

		for (feature in features)
			with(feature) {
				scope.onConfigurationEnded()
			}
	}


	internal object Key : RaptorComponentKey2<RaptorKtorRouteComponent> {

		override fun toString() = "route"
	}


	private class ConfigurationEndScope(parent: RaptorComponentConfigurationEndScope2) : RaptorKtorRouteFeatureConfigurationEndScope {

		private val routeScope = object :
			RaptorKtorRouteFeatureConfigurationEndScope.RouteScope,
			RaptorComponentConfigurationEndScope2 by parent {}


		override fun route(configuration: RaptorKtorRouteFeatureConfigurationEndScope.RouteScope.() -> Unit) {
			routeScope.configuration()
		}
	}


	private inner class ConfigurationStartScope : RaptorKtorRouteFeatureConfigurationStartScope {

		override val route: RaptorKtorRouteComponent
			get() = this@RaptorKtorRouteComponent
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorRouteComponent>.custom(configure: RaptorKtorRouteInitializationScope.() -> Unit) {
	this{
		custom(configure)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorRouteComponent>.install(feature: RaptorKtorRouteFeature) {
	this{
		install(feature)
	}
}


@RaptorDsl
public val RaptorAssemblyQuery2<RaptorKtorRouteComponent>.routes: RaptorAssemblyQuery2<RaptorKtorRoutesComponent>
	get() = map { it.routes }


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorRouteComponent>.wrap(wrapper: RaptorKtorRouteInitializationScope.(next: Route.() -> Unit) -> Unit) {
	this {
		wrap(wrapper)
	}
}
