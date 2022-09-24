package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


private val graphsPropertyKey = RaptorPropertyKey<Collection<RaptorGraph>>("graphs")


public class RaptorGraphsComponent internal constructor() : RaptorComponent.Base<RaptorGraphsComponent>(), RaptorComponentSet<RaptorGraphComponent> {

	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorGraphComponent>
		get() = componentRegistry.all(RaptorGraphComponent.key).all


	@RaptorDsl
	public fun new(): RaptorGraphComponent =
		RaptorGraphComponent()
			.also { componentRegistry.register(RaptorGraphComponent.key, it) }


	@RaptorDsl
	public fun new(configure: RaptorGraphComponent.() -> Unit = {}) {
		new().configure()
	}


	override fun RaptorComponentConfigurationEndScope<RaptorGraphsComponent>.onConfigurationEnded() {
		propertyRegistry.register(
			key = graphsPropertyKey,
			value = componentRegistry.many(RaptorGraphComponent.key).map { component ->
				component.endConfiguration()

				checkNotNull(component.graph)
			}
		)
	}


	internal companion object {

		val key = RaptorComponentKey<RaptorGraphsComponent>("graphs")
	}
}


// FIXME It's odd that we have to put it here just to keep the key private. This logically belongs to API.
//       Keep all keys shared within the module in a single file?
public val RaptorContext.graphs: Collection<RaptorGraph>
	get() = properties[graphsPropertyKey] ?: throw RaptorFeatureNotInstalledException(RaptorGraphFeature)


@RaptorDsl
public val RaptorTopLevelConfigurationScope.graphs: RaptorGraphsComponent
	get() = componentRegistry.oneOrNull(RaptorGraphsComponent.key) ?: throw RaptorFeatureNotInstalledException(RaptorGraphFeature)
