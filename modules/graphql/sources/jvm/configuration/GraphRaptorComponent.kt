package io.fluidsonic.raptor


// FIXME taggable
class GraphRaptorComponent internal constructor() : RaptorComponent.Default<GraphRaptorComponent>() {

	internal val definitions: MutableList<RaptorGraphDefinition> = mutableListOf()
	internal var includesDefaultDefinitions = false


	internal fun toGraphRoute() =
		GraphRoute(
			system = GraphSystem(definitions = definitions)
		)


	companion object;


	internal object Key : RaptorComponentKey<GraphRaptorComponent> {

		override fun toString() = "graphql"
	}
}


@RaptorDsl
fun RaptorComponentSet<GraphRaptorComponent>.definitions(vararg definitions: RaptorGraphDefinition) {
	definitions(definitions.asIterable())
}


@RaptorDsl
fun RaptorComponentSet<GraphRaptorComponent>.definitions(definitions: Iterable<RaptorGraphDefinition>) {
	configure {
		this.definitions += definitions
	}
}


@RaptorDsl
fun RaptorComponentSet<GraphRaptorComponent>.includeDefaultDefinitions() = configure {
	if (includesDefaultDefinitions)
		return@configure

	includesDefaultDefinitions = true

	definitions(RaptorGraphDefaults.definitions)
}
