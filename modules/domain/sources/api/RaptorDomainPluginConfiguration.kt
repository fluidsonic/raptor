package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*


public class RaptorDomainPluginConfiguration internal constructor(
	public val aggregates: RaptorAggregatesConfiguration,
	internal var onLoadedActions: List<suspend RaptorScope.() -> Unit>,
)
