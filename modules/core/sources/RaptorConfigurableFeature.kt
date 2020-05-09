package io.fluidsonic.raptor


interface RaptorConfigurableFeature<RootComponent : RaptorComponent> {

	fun RaptorFeatureFinalizationScope.finalizeConfigurable() = Unit
	fun RaptorFeatureInstallationScope.installConfigurable(): RaptorComponentKey<RootComponent>


	companion object
}
