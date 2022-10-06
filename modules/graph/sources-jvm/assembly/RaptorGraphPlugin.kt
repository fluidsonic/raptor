package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public object RaptorGraphPlugin : RaptorPluginWithConfiguration<RaptorGraphPluginConfiguration> {

	override fun RaptorPluginCompletionScope.complete(): RaptorGraphPluginConfiguration {
		completeComponents()

		return RaptorGraphPluginConfiguration(
			graphs = componentRegistry.one(Keys.graphsComponent).complete(),
		)
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.graphsComponent, RaptorGraphsComponent())
	}


	override fun toString(): String = "graph"
}
