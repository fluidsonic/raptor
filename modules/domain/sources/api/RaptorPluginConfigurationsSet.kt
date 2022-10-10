package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*


public val RaptorPluginConfigurationsSet.domain: RaptorDomainPluginConfiguration
	get() = get(RaptorDomainPlugin)
