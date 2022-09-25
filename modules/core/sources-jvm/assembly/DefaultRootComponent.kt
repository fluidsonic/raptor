package io.fluidsonic.raptor


// FIXME Note dependencies between plugins and finalize accordingly.
internal class DefaultRootComponent : RaptorComponent.Base<RaptorRootComponent>(), RaptorComponent<RaptorRootComponent>, RaptorRootComponent {

	private val componentRegistry = DefaultComponentRegistry()
	private var configurationEnded = false
	private val pluginConfigurators: MutableMap<RaptorPlugin, RaptorPluginConfigurator> = hashMapOf()
	private val lazyContext = LazyRootContext()
	private val propertyRegistry = DefaultPropertyRegistry()
	private val scopes = Scopes()


	init {
		componentRegistry.register(Keys.rootComponentKey, component = this)
	}


	fun assemble(configure: RaptorAssemblyInstallationScope.() -> Unit): Raptor {
		scopes.configure()

		return endConfiguration()
	}


	private fun checkConfigurable() {
		check(!configurationEnded) { "The configuration phase has already ended." }
	}


	private fun configurator(plugin: RaptorPlugin) =
		pluginConfigurators.getOrPut(plugin) {
			RaptorPluginConfigurator(
				completionScope = scopes,
				componentRegistry = componentRegistry,
				installationScope = scopes,
				plugin = plugin,
			)
		}


	internal fun endConfiguration(): Raptor {
		checkConfigurable()

		configurationEnded = true

		// FIXME Finalize components by plugin.
		for (configurator in pluginConfigurators.values)
			configurator.complete()

		componentRegistry.endConfiguration(scope = scopes)

		val context = DefaultRaptorContext(properties = propertyRegistry.toSet())
		val raptor = DefaultRaptor(context = context)

		lazyContext.resolve(context)

		return raptor
	}


	override fun toString() =
		"root"


	private inner class Scopes : RaptorPluginCompletionScope, RaptorPluginInstallationScope {

		override fun <Plugin : RaptorPlugin> complete(plugin: Plugin, action: RaptorPluginScope<Plugin>.() -> Unit) {
			// FIXME
			TODO("Not yet implemented")
		}


		override val componentRegistry: RaptorComponentRegistry
			get() = this@DefaultRootComponent.componentRegistry


		override fun install(plugin: RaptorPlugin) {
			this@DefaultRootComponent.checkConfigurable()
			this@DefaultRootComponent.configurator(plugin).install()
		}


		override val lazyContext: RaptorContext
			get() = this@DefaultRootComponent.lazyContext


		override fun optional(plugin: RaptorPlugin, action: () -> Unit) {
			this@DefaultRootComponent.checkConfigurable()
			this@DefaultRootComponent.configurator(plugin).configure(required = false, action = action)
		}


		override val propertyRegistry: RaptorPropertyRegistry
			get() = this@DefaultRootComponent.propertyRegistry


		override fun require(plugin: RaptorPlugin, action: () -> Unit) {
			this@DefaultRootComponent.checkConfigurable()
			this@DefaultRootComponent.configurator(plugin).configure(required = true, action = action)
		}
	}
}
