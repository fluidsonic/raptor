package tests

import io.fluidsonic.raptor.*


object TextCollectionFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(TextCollectionComponent.key, TextCollectionComponent())
	}


	override fun toString() =
		"text collection feature"
}
