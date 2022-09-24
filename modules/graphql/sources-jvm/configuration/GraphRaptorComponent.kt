package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*


public class GraphRaptorComponent internal constructor() :
	RaptorComponent.Base<GraphRaptorComponent>(),
	RaptorTaggableComponent<GraphRaptorComponent> {

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


	internal companion object {

		val key = RaptorComponentKey<GraphRaptorComponent>("graphql")
	}
}
