package tests

import io.fluidsonic.raptor.*


object NodeFeature : RaptorFeature.Configurable<NodeComponent> {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(RootNodeRaptorKey, componentRegistry.one(NodeComponent.Key).toNode())
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: NodeComponent.() -> Unit) {
		componentRegistry.oneOrRegister(NodeComponent.Key) { NodeComponent(name = "root") }.action()
	}


	override fun toString() =
		"node feature"
}
