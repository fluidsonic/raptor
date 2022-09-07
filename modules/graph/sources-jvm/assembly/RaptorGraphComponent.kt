package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public class RaptorGraphComponent internal constructor() : RaptorComponent2.Base(), RaptorTaggableComponent2 {

	private val definitions: MutableList<RaptorGraphDefinition> = mutableListOf()
	private var graph: RaptorGraph? = null
	private var includesDefaultDefinitions = false


	internal fun finalize(): RaptorGraph =
		checkNotNull(graph)


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


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		graph = GraphSystemDefinitionBuilder.build(definitions)
			.let(GraphTypeSystemBuilder::build)
			.let { GraphSystemBuilder.build(tags = tags(this@RaptorGraphComponent), typeSystem = it) }
	}


	internal object Key : RaptorComponentKey2<RaptorGraphComponent> {

		override fun toString() = "graph"
	}
}
