package tests

import io.fluidsonic.raptor.*


object NodeFeature : RaptorFeature.Configurable<NodeComponent> {

	override fun RaptorTopLevelConfigurationScope.configure(action: NodeComponent.() -> Unit) {
		componentRegistry.configure(key = NodeComponent.Key, action = action)
	}


	override fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(RootNodeRaptorKey, componentRegistry.one(NodeComponent.Key).toNode())
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(NodeComponent.Key, NodeComponent(name = "root"))
	}


	override fun toString() =
		"node feature"
}
