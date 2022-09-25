package tests

import io.fluidsonic.raptor.*


object NodePlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		propertyRegistry.register(Node.rootPropertyKey, componentRegistry.one(NodeComponent.key).toNode())
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(NodeComponent.key, NodeComponent(name = "root"))
	}


	override fun toString() =
		"node"
}


@RaptorDsl
val RaptorAssemblyInstallationScope.nodes: RaptorAssemblyQuery<NodeComponent>
	get() = componentRegistry.oneOrNull(NodeComponent.key) ?: throw RaptorPluginNotInstalledException(NodePlugin)
