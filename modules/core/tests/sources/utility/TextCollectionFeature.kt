package tests

import io.fluidsonic.raptor.*


object TextCollectionFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		propertyRegistry.register(TextRaptorPropertyKey, componentRegistry.one(TextCollectionComponent.Key).finalize())
	}


	override fun RaptorFeatureInstallationScope.install() {
		componentRegistry.register(TextCollectionComponent.Key, TextCollectionComponent())
	}


	override fun toString() =
		"text collection feature"
}
