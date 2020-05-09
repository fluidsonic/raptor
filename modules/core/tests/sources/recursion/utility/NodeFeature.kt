package tests

import io.fluidsonic.raptor.*


object NodeFeature : RaptorConfigurableFeature<NodeComponent> {

	override fun RaptorFeatureFinalizationScope.finalizeConfigurable(rootComponent: NodeComponent) {
		assign(RootNodeRaptorKey, rootComponent.finalize())
	}


	override fun RaptorFeatureInstallationScope.installConfigurable() =
		NodeComponent(name = "root", registry = registry)
}
