package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public object RaptorGraphFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		componentRegistry.register(RaptorGraphsComponent.key) { RaptorGraphsComponent() }
	}


	override fun toString(): String = "graph"
}
