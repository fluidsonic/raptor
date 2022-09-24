package tests

import io.fluidsonic.raptor.*


object NodeFeature : RaptorFeature.Configurable<NodeComponent> {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(Node.rootPropertyKey, componentRegistry.one(NodeComponent.key).toNode())
	}


	override fun RaptorFeatureConfigurationScope.beginConfiguration(action: NodeComponent.() -> Unit) {
		componentRegistry.oneOrRegister(NodeComponent.key) { NodeComponent(name = "root") }.action()
	}


	override fun toString() =
		"node feature"
}
