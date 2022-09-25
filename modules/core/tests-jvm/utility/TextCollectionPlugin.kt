package tests

import io.fluidsonic.raptor.*


object TextCollectionPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(TextCollectionComponent.key, TextCollectionComponent())
	}


	override fun toString() =
		"text collection feature"
}
