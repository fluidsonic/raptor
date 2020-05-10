package io.fluidsonic.raptor


internal class DefaultRaptorRootComponent : RaptorComponent.Default<RaptorRootComponent>(),
	RaptorRootComponent,
	RaptorComponentConfigurationEndScope,
	RaptorFeatureConfigurationEndScope,
	RaptorFeatureConfigurationStartScope {

	private val features: MutableSet<RaptorFeature> = mutableSetOf()

	override val componentRegistry = DefaultRaptorComponentRegistry()
	override val lazyContext = LazyRootRaptorContext()
	override val propertyRegistry = DefaultRaptorPropertyRegistry()


	init {
		componentRegistry.register(Key, component = this)
	}


	internal fun endConfiguration(): Raptor {
		componentRegistry.endConfiguration(scope = this)

		for (feature in features)
			with(feature) {
				onConfigurationEnded()
			}

		val context = DefaultRaptorContext(properties = propertyRegistry.toSet())
		val raptor = DefaultRaptor(context = context)

		lazyContext.resolve(context)

		return raptor
	}


	override fun install(feature: RaptorFeature) {
		if (features.add(feature))
			with(feature) {
				onConfigurationStarted()
			}
	}


	override fun <Feature : RaptorFeature.WithRootComponent<RootComponent>, RootComponent : RaptorComponent> install(
		feature: Feature,
		configuration: RootComponent.() -> Unit
	) {
		val rootComponentKey = with(feature) { rootComponentKey }

		if (features.add(feature)) {
			with(feature) {
				onConfigurationStarted()
			}

			checkNotNull(componentRegistry.oneOrNull(rootComponentKey)) {
				"Feature of type '${feature::class.qualifiedName}' must register its root component with key '$rootComponentKey' in 'onConfigurationStarted()'."
			}
		}

		componentRegistry.configure(rootComponentKey, configuration)
	}


	override fun toString() =
		"default core"


	private object Key : RaptorComponentKey<DefaultRaptorRootComponent> {

		override fun toString() = "core"
	}
}
