package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public class RaptorGraphsComponent internal constructor() : RaptorComponent2.Base(), RaptorComponentSet2<RaptorGraphComponent> {

	@RaptorDsl
	override fun all(configure: RaptorGraphComponent.() -> Unit) {
		componentRegistry2.all(RaptorGraphComponent.Key, configure)
	}


	@RaptorDsl
	public fun new(): RaptorGraphComponent =
		RaptorGraphComponent()
			.also { componentRegistry2.register(RaptorGraphComponent.Key, it) }


	@RaptorDsl
	public fun new(configure: RaptorGraphComponent.() -> Unit = {}) {
		new().configure()
	}


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		propertyRegistry.register(
			key = RaptorGraphsPropertyKey,
			value = componentRegistry2.many(RaptorGraphComponent.Key).map { component ->
				component.endConfiguration()

				checkNotNull(component.graph)
			}
		)
	}


	internal object Key : RaptorComponentKey2<RaptorGraphsComponent> {

		override fun toString() = "graphs"
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.graphs: RaptorGraphsComponent
	get() = componentRegistry2.oneOrNull(RaptorGraphsComponent.Key) ?: throw RaptorFeatureNotInstalledException(RaptorGraphFeature)
