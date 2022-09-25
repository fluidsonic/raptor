package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*


@RaptorDsl
public class RaptorDomainComponent internal constructor(
	private val topLevelScope: RaptorTopLevelConfigurationScope, // FIXME remove hack
) : RaptorComponent.Base<RaptorDomainComponent>() {

	@RaptorDsl
	public val aggregates: RaptorAggregatesComponent
		get() = componentRegistry.one(Keys.aggregatesComponent)


	// FIXME rework & standardize
	internal fun complete(): RaptorDomain =
		RaptorDomain(aggregates = aggregates.complete())


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(Keys.aggregatesComponent, RaptorAggregatesComponent(topLevelScope = topLevelScope))
	}
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorDomainComponent>.aggregates: RaptorAssemblyQuery<RaptorAggregatesComponent>
	get() = map { it.aggregates }
