package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.server.routing.*


private val routesComponentKey = RaptorComponentKey<RaptorKtorRoutesComponent.NonRoot>("routes")


// FIXME needs custom property set scope & hierarchy
//FIXME continue here
public class RaptorKtorRouteComponent internal constructor(
	private val host: String?,
	private val path: String,
) : RaptorComponent.Base<RaptorKtorRouteComponent>(),
	RaptorTaggableComponent<RaptorKtorRouteComponent>,
	RaptorTransactionBoundary<RaptorKtorRouteComponent> {

	private var configuration: KtorRouteConfiguration? = null
	private val customConfigurations = mutableListOf<Route.() -> Unit>()
	private val plugins = mutableSetOf<RaptorKtorRoutePlugin>()
	private var wrapper: (Route.(next: Route.() -> Unit) -> Unit)? = null


	// FIXME Rethink architecture.
	//       At this point we need a per-route propertyRegistry (hierarchical) and transactionFactory (hierarchical).
	//       Standardize RaptorTransactionGeneratingComponent -> RaptorTransactionBoundary and add RaptorPropertyBoundary.
	//       Make sure that RaptorComponentConfigurationStartScope2 and RaptorComponentConfigurationEndScope2 are consistent and component-bound.
	//       Allow requiring the completed configuration of other plugins (in end scope) and detect cycles.
	//       Eventually force scope for referencing other plugins, e.g. complete(RaptorGraphPlugin) { graph(tag) }.
	//       (handy once we have context receivers to extend other types)
	//       Alternatively make each plugin have it's own shortcut for "require and use", e.g. val graphPlugin = use(plugins.graph)
	//       or make plugins.graph.… automatically finalize its configurations.
	//       How do we know what component belongs to what plugin?
	//       Should we allow plugin configuration on installation? Rarely needed & adds complexity.
	//       Should we allow dynamic plugins? I.e. class instead of object. How does that affect API?
	//       Should we allow plugins to be installed at defined boundaries?
	//           e.g. RaptorKtorRoutePlugin : RaptorBoundaryPlugin<RaptorKtorRouteComponent>, class RaptorKtorRouteComponent: RaptorPluginBoundary
	//       Make all component companions internal and use them for component definition? (label, key, "return type")
	internal fun complete() = checkNotNull(configuration)


	@RaptorDsl
	public fun custom(configure: RaptorKtorRouteInitializationScope.() -> Unit) {
		customConfigurations += configure
	}


	@RaptorDsl
	public fun install(plugin: RaptorKtorRoutePlugin) {
		if (plugins.add(plugin))
			with(plugin) {
				ConfigurationStartScope().onConfigurationStarted()
			}
	}


	@RaptorDsl
	public val routes: RaptorKtorRoutesComponent<*>
		get() = componentRegistry.oneOrRegister(routesComponentKey) { RaptorKtorRoutesComponent.NonRoot() }


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


	// FIXME use DSL instead of overridden functions? see Ktor 2
	override fun RaptorComponentConfigurationEndScope<RaptorKtorRouteComponent>.onConfigurationEnded() {
		if (plugins.isNotEmpty()) {
			val scope = ConfigurationEndScope(parent = this)

			for (plugin in plugins)
				with(plugin) {
					scope.onConfigurationEnded()
				}
		}

		// TODO Check/clean paths.
		configuration = KtorRouteConfiguration(
			children = componentRegistry.oneOrNull(routesComponentKey)?.complete().orEmpty(),
			customConfigurations = customConfigurations.toList(),
			host = host,
			path = path,
			properties = propertyRegistry.toSet(),
			transactionFactory = transactionFactory(), // FIXME use component-bound scope
			wrapper = wrapper,
		)
	}


	private class ConfigurationEndScope(parent: RaptorComponentConfigurationEndScope<RaptorKtorRouteComponent>) : RaptorKtorRoutePluginConfigurationEndScope {

		private val routeScope = object :
			RaptorKtorRoutePluginConfigurationEndScope.RouteScope,
			RaptorComponentConfigurationEndScope<RaptorKtorRouteComponent> by parent {}


		override fun route(configuration: RaptorKtorRoutePluginConfigurationEndScope.RouteScope.() -> Unit) {
			routeScope.configuration()
		}
	}


	private inner class ConfigurationStartScope : RaptorKtorRoutePluginConfigurationStartScope {

		override val route: RaptorKtorRouteComponent
			get() = this@RaptorKtorRouteComponent
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRouteComponent>.custom(configure: RaptorKtorRouteInitializationScope.() -> Unit) {
	this{
		custom(configure)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRouteComponent>.install(plugin: RaptorKtorRoutePlugin) {
	this{
		install(plugin)
	}
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorKtorRouteComponent>.routes: RaptorAssemblyQuery<RaptorKtorRoutesComponent<*>>
	get() = map { it.routes }


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRouteComponent>.wrap(wrapper: RaptorKtorRouteInitializationScope.(next: Route.() -> Unit) -> Unit) {
	this {
		wrap(wrapper)
	}
}
