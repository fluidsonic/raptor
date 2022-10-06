package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public val RaptorPluginConfigurationsSet.graph: RaptorGraphPluginConfiguration
	get() = get(RaptorGraphPlugin)
