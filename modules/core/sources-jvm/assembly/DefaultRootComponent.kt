package io.fluidsonic.raptor


internal class DefaultRootComponent : RaptorComponent.Base<RaptorRootComponent>(),
	RaptorComponent<RaptorRootComponent>,
	RaptorConfigurationEndScope,
	RaptorFeatureConfigurationApplicationScope,
	RaptorFeatureConfigurationScope,
	RaptorRootComponent {

	private var configurationEnded = false
	private val featureConfigurators: MutableMap<RaptorFeature, FeatureConfigurator> = hashMapOf()

	override val componentRegistry = DefaultComponentRegistry()
	override val lazyContext = LazyRootContext()
	override val propertyRegistry = DefaultPropertyRegistry()


	init {
		componentRegistry.register(Keys.rootComponentKey, component = this)
	}


	internal fun endConfiguration(): Raptor {
		requireConfigurable()

		configurationEnded = true

		for (configurator in featureConfigurators.values)
			configurator.completeConfiguration()

		componentRegistry.endConfiguration(scope = this)

		for (configurator in featureConfigurators.values)
			configurator.endConfiguration()

		val context = DefaultRaptorContext(properties = propertyRegistry.toSet())
		val raptor = DefaultRaptor(context = context)

		lazyContext.resolve(context)

		return raptor
	}


	private fun featureConfigurator(feature: RaptorFeature) =
		featureConfigurators.getOrPut(feature) { FeatureConfigurator(feature) }


	override fun ifFeature(feature: RaptorFeature, action: () -> Unit) {
		requireConfigurable()
		featureConfigurator(feature).configure(required = false, action = action)
	}


	override fun install(feature: RaptorFeature) {
		requireConfigurable()
		featureConfigurator(feature).install()
	}


	override fun <Feature : RaptorFeature.Configurable<ConfigurationScope>, ConfigurationScope : Any> install(
		feature: Feature,
		configuration: ConfigurationScope.() -> Unit,
	) {
		install(feature)

		with(feature) {
			beginConfiguration(configuration)
		}
	}


	private fun requireConfigurable() {
		check(!configurationEnded) { "The configuration phase has already ended." }
	}


	override fun requireFeature(feature: RaptorFeature, action: () -> Unit) {
		requireConfigurable()
		featureConfigurator(feature).configure(required = true, action = action)
	}


	override fun RaptorComponent<*>.endConfiguration() {
		// TODO Refactor.
		registration.endConfiguration(this@DefaultRootComponent)
	}


	override fun toString() =
		"root"


	private inner class FeatureConfigurator(private val feature: RaptorFeature) {

		private var configurations: MutableList<() -> Unit>? = null
		private var isInstalled = false
		private var notInstalledException: RaptorFeatureNotInstalledException? = null


		fun configure(required: Boolean, action: () -> Unit) {
			when (isInstalled) {
				true -> action()
				false -> {
					if (required && notInstalledException == null)
						notInstalledException = RaptorFeatureNotInstalledException(feature)

					ensureConfigurations().add(action)
				}
			}
		}


		private fun applyConfigurations() {
			val configurations = configurations ?: return
			this.configurations = null

			for (configure in configurations)
				configure()

			// Configurations may have added new configurations.
			applyConfigurations()
		}


		private fun beginConfiguration() {
			with(feature) {
				this@DefaultRootComponent.installed()
			}
		}


		fun completeConfiguration() {
			if (!isInstalled) {
				notInstalledException?.let { throw it }
				return
			}

			with(feature) {
				this@DefaultRootComponent.completeConfiguration()
			}
		}


		fun endConfiguration() {
			if (!isInstalled)
				return

			with(feature) {
				this@DefaultRootComponent.applyConfiguration()
			}
		}


		private fun ensureConfigurations() =
			configurations ?: mutableListOf<() -> Unit>().also { configurations = it }


		fun install() {
			if (isInstalled)
				return

			isInstalled = true
			notInstalledException = null

			beginConfiguration()
		}
	}
}
