package io.fluidsonic.raptor

import kotlin.reflect.*


internal class DefaultRaptorCoreComponent : RaptorComponent.Base<RaptorCoreComponent>(),
	RaptorCoreComponent,
	RaptorFeatureFinalizationScope,
	RaptorFeatureInstallationScope {

	private val featureInstallations: MutableList<FeatureInstallation<*, *>> = mutableListOf()
	private val properties: MutableMap<RaptorKey<*>, Any> = hashMapOf()
	private val _registry = DefaultRaptorComponentRegistry()


	init {
		registry.register(this)
	}


	override fun <Value : Any> assign(key: RaptorKey<Value>, value: Value) {
		properties.putIfAbsent(key, value)?.let { existingValue ->
			error(
				"Cannot assign value to key '$key' as one has already been assigned.\n" +
					"\tExisting value: $existingValue\n" +
					"\tValue to be assigned: $value"
			)
		}
	}


	override fun <Component : RaptorComponent> components(type: KClass<Component>): Collection<Component> =
		_registry.registeredComponents(type)


	internal fun finalize(): Raptor {
		for (installation in featureInstallations)
			installation.performFinalize(scope = this)

		return DefaultRaptor(properties = properties)
	}


	@Suppress("UNCHECKED_CAST")
	override fun <Feature : RaptorConfigurableFeature<Component>, Component : RaptorComponent> install(
		feature: Feature,
		rootComponentType: KClass<Component>,
		configure: Component.() -> Unit
	) {
		val installation = featureInstallations
			.firstOrNull { it.feature == feature }
			?.let { it as FeatureInstallation<Feature, Component> }
			?: run {
				FeatureInstallation(feature = feature)
					.also { installation ->
						featureInstallations += installation

						installation.performInstall(scope = this)
						registry.register(installation.rootComponent, type = rootComponentType)
					}
			}

		installation.rootComponent.configure()
	}


	override val registry: RaptorComponentRegistry
		get() = _registry


	private class FeatureInstallation<Feature : RaptorConfigurableFeature<RootComponent>, RootComponent : RaptorComponent>(
		val feature: Feature
	) {

		lateinit var rootComponent: RootComponent
			private set


		fun performFinalize(scope: RaptorFeatureFinalizationScope) {
			with(feature) {
				scope.finalizeConfigurable(rootComponent = rootComponent)
			}
		}


		fun performInstall(scope: RaptorFeatureInstallationScope) {
			with(feature) {
				rootComponent = scope.installConfigurable()
			}
		}
	}
}
