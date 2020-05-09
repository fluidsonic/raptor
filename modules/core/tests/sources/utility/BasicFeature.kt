package tests

import io.fluidsonic.raptor.*


object BasicFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		assign(BasicRaptorKey, component<BasicComponent>()._text)
	}


	override fun RaptorFeatureInstallationScope.install() {
		registry.register(BasicComponent())
	}
}
