package io.fluidsonic.raptor


internal class RaptorAssembly(
	configure: RaptorAssemblyInstallationScope.() -> Unit,
) {

	private var state = State.installation

	val raptor: Raptor


	init {
		raptor = Installation(configure)
			.let { installation ->
				Completion(
					componentRegistry = installation.componentRegistry,
					plugins = installation.plugins,
				)
			}
			.raptor
	}


	private inner class Completion(
		private val componentRegistry: DefaultComponentRegistry,
		plugins: Set<RaptorPluginWithConfiguration<*>>,
	) {

		private val lazyContext = LazyRootContext()
		private val propertyRegistry = DefaultPropertyRegistry()

		private val pluginConfigurators: Map<RaptorPluginWithConfiguration<*>, PluginConfigurator<*, *>> =
			plugins.associateWithTo(hashMapOf(), ::PluginConfigurator)

		val raptor: Raptor


		init {
			for (configurator in pluginConfigurators.values)
				configurator.complete()

			val context = DefaultRaptorContext(properties = propertyRegistry.toSet())
			raptor = DefaultRaptor(context = context)

			lazyContext.resolve(context)
		}


		@Suppress("UNCHECKED_CAST")
		private fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> requireConfigurator(
			plugin: Plugin,
		) =
			pluginConfigurators[plugin]
				as PluginConfigurator<Configuration, Plugin>?
				?: throw RaptorPluginNotInstalledException(plugin)


		private inner class PluginConfigurator<out Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>>(
			val plugin: RaptorPluginWithConfiguration<Configuration>,
		) : RaptorPluginCompletionScope, RaptorPluginScope<RaptorPluginWithConfiguration<*>> {

			private var configuration: Configuration? = null
			private var isCompleting = false

			override val componentRegistry: DefaultComponentRegistry get() = this@Completion.componentRegistry
			override val lazyContext: RaptorContext get() = this@Completion.lazyContext
			override val propertyRegistry: RaptorPropertyRegistry get() = this@Completion.propertyRegistry


			private fun configureBy(
				plugin: RaptorPluginWithConfiguration<*>,
				action: RaptorPluginScope<Plugin>.() -> Unit,
			) {
				check(configuration == null) { "Plugin $plugin cannot configure plugin ${this.plugin} as its assembly was already completed." }

				@Suppress("UNCHECKED_CAST")
				(this as RaptorPluginScope<Plugin>).action()
			}


			// FIXME Detect and report cycles.
			override fun <OtherPlugin : RaptorPluginWithConfiguration<*>> configure(
				plugin: OtherPlugin,
				action: RaptorPluginScope<OtherPlugin>.() -> Unit,
			) {
				check(configuration == null) { "Plugin $plugin cannot configure any plugins as its assembly was already completed." }

				requireConfigurator(plugin).configureBy(this@PluginConfigurator.plugin, action)
			}


			fun complete(): Configuration {
				configuration?.let { return it }

				// TODO This is just a very basic protection. Improve.
				check(!isCompleting) { "Plugin completion cycle detected." }
				isCompleting = true

				// TODO In the future we could complete the components after the plugin and allow the plugin to force-complete using scope.completeComponents().
				// FIXME Won't work properly. A component of the plugin might depend on plugin dependencies to be completed first.
				//       But we'll only know about that below.
				//       Potential solutions:
				//       - Require dependencies to also be declared in install(). Then complete them before completing the components.
				//         -> Won't work as we may need to use configure() in the plugin completion.
				//       - Complete components afterwards unless the plugin explicitly uses scope.completeComponents().
				//       - Never complete components automatically. Plugin must navigate the hierarchy top-down and provide a scope which allows
				//         component-specific access using e.g. scope.graph(component). Downside is that we cannot automatically detect references
				//         to components that have not been completed yet.
				//       - Never allow components to depend on each other. Only support communication through properties or plugins configurations.
				componentRegistry.complete(plugin = plugin, scope = this)

				return with(plugin) {
					(this@PluginConfigurator as RaptorPluginCompletionScope).complete()
				}.also { configuration = it }
			}


			// FIXME Detect and report cycles.
			override fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> require(
				plugin: Plugin,
				action: (configuration: Configuration) -> Unit,
			) {
				check(configuration == null) { "Plugin $plugin cannot configure any plugins as its assembly was already completed." }

				action(requireConfigurator(plugin).complete())
			}
		}
	}


	private inner class Installation(
		configure: RaptorAssemblyInstallationScope.() -> Unit,
	) : RaptorAssemblyInstallationScope {

		private var isCompleting = false
		private val pluginConfigurators: MutableMap<RaptorPluginWithConfiguration<*>, PluginConfigurator> = hashMapOf()

		override val componentRegistry = DefaultComponentRegistry()


		init {
			configure()

			isCompleting = true

			for (configurator in pluginConfigurators.values)
				configurator.complete()
		}


		private fun configurator(plugin: RaptorPluginWithConfiguration<*>) =
			pluginConfigurators.getOrPut(plugin) {
				PluginConfigurator(plugin = plugin)
			}


		override fun install(plugin: RaptorPluginWithConfiguration<*>) {
			check(!isCompleting) { "Cannot install plugin $plugin once the assembly is completing." }

			configurator(plugin).install()
		}


		override fun optional(plugin: RaptorPluginWithConfiguration<*>, action: () -> Unit) {
			check(!isCompleting) { "Cannot configure plugin $plugin once the assembly is completing." }

			configurator(plugin).configure(required = false, action = action)
		}


		val plugins: Set<RaptorPluginWithConfiguration<*>>
			get() = pluginConfigurators.values
				.filter { it.isInstalled }
				.mapTo(hashSetOf()) { it.plugin }


		override fun require(plugin: RaptorPluginWithConfiguration<*>, action: () -> Unit) {
			check(!isCompleting) { "Cannot configure plugin $plugin once the assembly is completing." }

			configurator(plugin).configure(required = true, action = action)
		}


		private inner class PluginConfigurator(
			val plugin: RaptorPluginWithConfiguration<*>,
		) : RaptorPluginInstallationScope {

			private var notInstalledException: RaptorPluginNotInstalledException? = null
			private var pendingActions: MutableList<() -> Unit>? = null

			var isInstalled = false
				private set

			override val componentRegistry: RaptorComponentRegistry get() = this@Installation.componentRegistry


			private fun applyPendingActions() {
				val actions = pendingActions ?: return
				pendingActions = null

				for (action in actions)
					action()
			}


			fun complete() {
				if (!isInstalled)
					notInstalledException?.let { throw it }
			}


			fun configure(required: Boolean, action: () -> Unit) {
				when (isInstalled) {
					true -> action()
					false -> {
						if (required && notInstalledException == null)
							notInstalledException = RaptorPluginNotInstalledException(plugin)

						(pendingActions ?: mutableListOf<() -> Unit>().also { pendingActions = it })
							.add(action)
					}
				}
			}


			fun install() {
				if (isInstalled)
					return

				isInstalled = true
				notInstalledException = null

				with(plugin) {
					(this@PluginConfigurator as RaptorPluginInstallationScope).install()
				}

				applyPendingActions()
			}


			override fun install(plugin: RaptorPluginWithConfiguration<*>) {
				this@Installation.install(plugin)
			}


			override fun optional(plugin: RaptorPluginWithConfiguration<*>, action: () -> Unit) {
				this@Installation.optional(plugin, action)
			}


			override fun require(plugin: RaptorPluginWithConfiguration<*>, action: () -> Unit) {
				this@Installation.require(plugin, action)
			}
		}
	}


	private enum class State {

		completed,
		completion,
		installation,
	}
}


@RaptorDsl
public fun raptor(configure: RaptorAssemblyInstallationScope.() -> Unit): Raptor =
	RaptorAssembly(configure).raptor
