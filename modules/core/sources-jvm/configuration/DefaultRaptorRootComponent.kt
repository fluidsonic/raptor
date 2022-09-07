package io.fluidsonic.raptor


internal class DefaultRaptorRootComponent : RaptorComponent.Default<RaptorRootComponent>(),
	RaptorComponent2,
	RaptorComponentConfigurationEndScope,
	RaptorComponentConfigurationEndScope2,
	RaptorFeatureConfigurationApplicationScope,
	RaptorFeatureConfigurationScope,
	RaptorRootComponent {

	private val featureIds: MutableSet<RaptorFeatureId> = mutableSetOf()
	private val features: MutableSet<RaptorFeature> = mutableSetOf()
	private val lazyFeatureConfigurations: MutableMap<RaptorFeatureId, MutableList<() -> Unit>> = hashMapOf()

	override val componentRegistry = DefaultRaptorComponentRegistry()
	override val componentRegistry2 = DefaultRaptorComponentRegistry2()
	override val lazyContext = LazyRootRaptorContext()
	override val propertyRegistry = DefaultRaptorPropertyRegistry()


	init {
		componentRegistry.register(Key, component = this)
		componentRegistry2.register(Key2, component = this)
	}


	private fun applyLazyFeatureConfigurations(featureId: RaptorFeatureId) {
		val pendingConfigurations = lazyFeatureConfigurations.remove(featureId)
		if (pendingConfigurations != null)
			for (configuration in pendingConfigurations)
				configuration()
	}


	internal fun endConfiguration(): Raptor {
		// FIXME prevent installing more features
		for (feature in features)
			with(feature) {
				completeConfiguration()
			}

		componentRegistry.endConfiguration(scope = this)
		componentRegistry2.endConfiguration(scope = this)

		for (feature in features)
			with(feature) {
				applyConfiguration()
			}

		val context = DefaultRaptorContext(properties = propertyRegistry.toSet())
		val raptor = DefaultRaptor(context = context)

		lazyContext.resolve(context)

		return raptor
	}


	override fun install(feature: RaptorFeature) {
		if (!features.add(feature))
			return

		val id = feature.id
		if (id != null && !featureIds.add(id))
			error("Multiple features cannot have the same ID '$id'.") // FIXME report which ones

		with(feature) {
			beginConfiguration()
		}

		id?.let(this::applyLazyFeatureConfigurations)
	}


	override fun <Feature : RaptorFeature.Configurable<ConfigurationScope>, ConfigurationScope : Any> install(
		feature: Feature,
		configuration: ConfigurationScope.() -> Unit,
	) {
		install(feature = feature)

		with(feature) { beginConfiguration(configuration) }
	}


	override fun toString() =
		"default core"


	override fun Unit.ifInstalled(featureId: RaptorFeatureId, action: () -> Unit) {
		if (featureIds.contains(featureId))
			action()
		else
			lazyFeatureConfigurations.getOrPut(featureId) { mutableListOf() }.add(action)
	}


	private object Key : RaptorComponentKey<DefaultRaptorRootComponent> {

		override fun toString() = "core"
	}


	private object Key2 : RaptorComponentKey2<DefaultRaptorRootComponent> {

		override fun toString() = "core"
	}
}
