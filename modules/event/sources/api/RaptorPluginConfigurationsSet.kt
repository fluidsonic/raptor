package io.fluidsonic.raptor.event

import io.fluidsonic.raptor.*


public val RaptorPluginConfigurationsSet.event: RaptorEventPluginConfiguration
	get() = get(RaptorEventPlugin)
