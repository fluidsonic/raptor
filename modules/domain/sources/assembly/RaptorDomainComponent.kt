package io.fluidsonic.raptor.domain

import io.fluidsonic.raptor.*


@RaptorDsl
public class RaptorDomainComponent internal constructor(
	private val topLevelScope: RaptorAssemblyInstallationScope, // FIXME remove hack
) : RaptorComponent.Base<RaptorDomainComponent>(RaptorDomainPlugin) {

	private val onLoadedActions: MutableList<suspend RaptorScope.() -> Unit> = mutableListOf()


	@RaptorDsl
	public val aggregates: RaptorAggregatesComponent
		get() = componentRegistry.one(Keys.aggregatesComponent)


	// TODO rework & standardize
	internal fun complete(context: RaptorContext): RaptorDomainPluginConfiguration =
		RaptorDomainPluginConfiguration(
			aggregates = aggregates.complete(context = context),
			onLoadedActions = onLoadedActions.toList(),
		)


	@RaptorDsl
	public fun onLoaded(action: suspend RaptorScope.() -> Unit) {
		onLoadedActions += action
	}


	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(Keys.aggregatesComponent, RaptorAggregatesComponent(topLevelScope = topLevelScope))
	}
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorDomainComponent>.aggregates: RaptorAssemblyQuery<RaptorAggregatesComponent>
	get() = map { it.aggregates }


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorDomainComponent>.onLoaded(action: suspend RaptorScope.() -> Unit) {
	each { onLoaded(action) }
}
