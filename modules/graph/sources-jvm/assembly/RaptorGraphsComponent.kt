package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public class RaptorGraphsComponent internal constructor(
	private val componentRegistry: RaptorComponentRegistry2,
) : RaptorComponent2.Base(), RaptorComponentSet2<RaptorGraphComponent> by componentRegistry.all(RaptorGraphComponent.Key) {

	@RaptorDsl
	public fun new(): RaptorGraphComponent =
		RaptorGraphComponent()
			.also { componentRegistry.register(RaptorGraphComponent.Key, it) }


	@RaptorDsl
	public fun new(configure: RaptorGraphComponent.() -> Unit = {}) {
		new().configure()
	}


	internal object Key : RaptorComponentKey2<RaptorGraphsComponent> {

		override fun toString() = "graphs"
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.graphs: RaptorGraphsComponent
	get() = componentRegistry2.oneOrRegister(RaptorGraphsComponent.Key) { RaptorGraphsComponent(componentRegistry2) }
