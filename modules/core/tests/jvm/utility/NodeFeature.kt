package tests

import io.fluidsonic.raptor.*


object NodeFeature : RaptorFeature.Configurable<NodeComponent> {

	override fun RaptorTopLevelConfigurationScope.configure(action: NodeComponent.() -> Unit) {
		componentRegistry.configure(key = NodeComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(RootNodeRaptorKey, componentRegistry.one(NodeComponent.Key).toNode())
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(NodeComponent.Key, NodeComponent(name = "root"))
	}


	override fun toString() =
		"node feature"
}
