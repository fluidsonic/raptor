package tests

import io.fluidsonic.raptor.*


object NodeFeature : RaptorConfigurableFeature<NodeComponent> {

	override fun RaptorFeatureFinalizationScope.finalizeConfigurable() {
		propertyRegistry.register(RootNodeRaptorKey, componentRegistry.one(NodeComponent.Key).finalize())
	}


	override fun RaptorFeatureInstallationScope.installConfigurable(): RaptorComponentKey<NodeComponent> {
		componentRegistry.register(NodeComponent.Key, NodeComponent(name = "root"))

		return NodeComponent.Key
	}


	override fun toString() =
		"node feature"
}
