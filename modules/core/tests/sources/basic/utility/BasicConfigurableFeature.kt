package tests

import io.fluidsonic.raptor.*


object BasicConfigurableFeature : RaptorConfigurableFeature<BasicComponent> {

	override fun RaptorFeatureFinalizationScope.finalizeConfigurable(rootComponent: BasicComponent) {
		assign(BasicRaptorKey, rootComponent.text)
	}


	override fun RaptorFeatureInstallationScope.installConfigurable() =
		BasicComponent()
}
