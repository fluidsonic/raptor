package io.fluidsonic.raptor


internal class DefaultRaptorFeatureRootComponent : RaptorComponent.Base<DefaultRaptorFeatureRootComponent>() {

	override fun toString() =
		"default feature root component"


	data class Key<Feature : RaptorConfigurableFeature<RootComponent>, RootComponent : RaptorComponent>(
		val feature: Feature
	) : RaptorComponentKey<RootComponent> {

		override fun toString() =
			"$feature root"
	}
}
