package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public object RaptorGraphPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(RaptorGraphsComponent.key) { RaptorGraphsComponent() }
	}


	override fun toString(): String = "graph"
}
