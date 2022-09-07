package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public object RaptorGraphFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorGraphFeatureId


	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(RaptorGraphsPropertyKey, componentRegistry2.many(RaptorGraphComponent.Key).map { it.finalize() })
	}
}


public const val raptorGraphFeatureId: RaptorFeatureId = "raptor.graph"
