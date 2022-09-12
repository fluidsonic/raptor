package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public object RaptorGraphFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		componentRegistry2.register(RaptorGraphsComponent.Key) { RaptorGraphsComponent() }
	}


	override fun toString(): String = "graph"
}
