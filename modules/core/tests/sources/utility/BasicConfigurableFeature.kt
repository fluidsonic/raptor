package tests

import io.fluidsonic.raptor.*


object BasicConfigurableFeature : RaptorConfigurableFeature<BasicComponent> {

	override fun RaptorFeatureFinalizationScope.finalizeConfigurable(rootComponent: BasicComponent) {
		assign(BasicRaptorKey, rootComponent._text)
	}


	override fun RaptorFeatureInstallationScope.installConfigurable() =
		BasicComponent()
}
