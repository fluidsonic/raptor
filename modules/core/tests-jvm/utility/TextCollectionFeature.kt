package tests

import io.fluidsonic.raptor.*


object TextCollectionFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(TextCollectionComponent.Key, TextCollectionComponent())
	}


	override fun toString() =
		"text collection feature"
}
