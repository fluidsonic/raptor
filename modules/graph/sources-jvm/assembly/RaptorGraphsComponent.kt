package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public class RaptorGraphsComponent internal constructor() :
	RaptorComponent.Base<RaptorGraphsComponent>(RaptorGraphPlugin),
	RaptorComponentSet<RaptorGraphComponent> {

	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorGraphComponent>
		get() = componentRegistry.all(Keys.graphComponent).all


	internal fun complete(): Collection<RaptorGraph> =
		componentRegistry.many(Keys.graphComponent).map { it.complete() }


	@RaptorDsl
	public fun new(): RaptorGraphComponent =
		componentRegistry.register(Keys.graphComponent, RaptorGraphComponent())


	@RaptorDsl
	public fun new(configure: RaptorGraphComponent.() -> Unit) {
		new().configure()
	}
}


@RaptorDsl
public val RaptorPluginScope<in RaptorGraphPlugin>.graphs: RaptorGraphsComponent
	get() = componentRegistry.oneOrNull(Keys.graphsComponent) ?: throw RaptorPluginNotInstalledException(RaptorGraphPlugin)
