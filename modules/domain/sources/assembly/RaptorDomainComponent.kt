package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*


@RaptorDsl
public class RaptorDomainComponent internal constructor(
	private val topLevelScope: RaptorTopLevelConfigurationScope, // FIXME remove hack
) : RaptorComponent.Base<RaptorDomainComponent>() {

	@RaptorDsl
	public val aggregates: RaptorAggregatesComponent
		get() = componentRegistry.oneOrRegister(Keys.aggregatesComponent) { RaptorAggregatesComponent(topLevelScope = topLevelScope) }


	// FIXME rework & standardize
	internal fun complete(): RaptorDomain =
		RaptorDomain(aggregateDefinitions = componentRegistry.oneOrNull(Keys.aggregatesComponent)?.complete().orEmpty())
}
