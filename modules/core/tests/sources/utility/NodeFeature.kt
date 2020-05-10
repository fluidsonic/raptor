package tests

import io.fluidsonic.raptor.*


object NodeFeature : RaptorFeature.WithRootComponent<NodeComponent> {

	override fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(RootNodeRaptorKey, componentRegistry.one(NodeComponent.Key).toNode())
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(NodeComponent.Key, NodeComponent(name = "root"))
	}


	override val RaptorFeatureConfigurationStartScope.rootComponentKey: RaptorComponentKey<out NodeComponent>
		get() = NodeComponent.Key


	override fun toString() =
		"node feature"
}
