package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*


// FIXME taggable
public class GraphRaptorComponent internal constructor() : RaptorComponent2.Base(), RaptorTaggableComponent2 {

	private val definitions: MutableList<RaptorGraphDefinition> = mutableListOf()
	private var includesDefaultDefinitions = false


	@RaptorDsl
	public fun definitions(vararg definitions: RaptorGraphDefinition) {
		definitions(definitions.asIterable())
	}


	@RaptorDsl
	public fun definitions(definitions: Iterable<RaptorGraphDefinition>) {
		this.definitions += definitions
	}


	@RaptorDsl
	public fun includeDefaultDefinitions() {
		if (includesDefaultDefinitions)
			return

		includesDefaultDefinitions = true

		definitions(RaptorGraphDefaults.definitions)
	}


	internal fun toGraphRoute() =
		GraphRoute(
			system = GraphSystemDefinitionBuilder.build(definitions)
				.let(GraphTypeSystemBuilder::build)
				.let(GraphSystemBuilder::build)
		)


	public companion object;


	internal object Key : RaptorComponentKey2<GraphRaptorComponent> {

		override fun toString() = "graphql"
	}
}
