package io.fluidsonic.raptor


internal class DefaultRaptorFeatureInstallationComponent<Feature : RaptorConfigurableFeature<RootComponent>, RootComponent : RaptorComponent>(
	private val feature: Feature,
	private val registry: RaptorComponentRegistry
) : RaptorComponent.Base<DefaultRaptorFeatureInstallationComponent<Feature, RootComponent>>() {

	private var rootComponentKey: RaptorComponentKey<RootComponent>? = null


	fun runConfiguration(action: RootComponent.() -> Unit) {
		val rootComponentKey = checkNotNull(rootComponentKey) { "Cannot configure feature that has not been installed." }

		registry.one(rootComponentKey).action()
	}


	fun runFinalization(scope: RaptorFeatureFinalizationScope) {
		with(feature) {
			scope.finalizeConfigurable()
		}
	}


	fun runInstallation(scope: RaptorFeatureInstallationScope) {
		with(feature) {
			rootComponentKey = scope.installConfigurable().also { key ->
				checkNotNull(registry.oneOrNull(key)) {
					"Feature of type ${feature::class.qualifiedName} must register its root component using key '$key' during installation."
				}
			}
		}
	}


	override fun toString() =
		"default feature component"
}
