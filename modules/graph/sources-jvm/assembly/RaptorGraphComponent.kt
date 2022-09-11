package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public class RaptorGraphComponent internal constructor() : RaptorComponent2.Base(), RaptorTaggableComponent2 {

	private val definitions: MutableList<RaptorGraphDefinition> = mutableListOf()
	private var includesDefaultDefinitions = false

	internal var graph: RaptorGraph? = null
		private set


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


// FIXME Other functions won't reliably resolve the right graph. It depends on the order that components are completed.
// We can either use two phases:
//  1. complete conf (create RaptorGraph)
//  2. end conf (reference RaptorGraph from other component)
// or we add some form of dependency system
// or we request an early configuration end on-demand (can lead to cycles which must be detected)
// Note that it's not yet possible to define graphs below the root component. But it might be at some point.
// FIXME might need requireFeature?
@RaptorDsl
public fun RaptorComponentConfigurationEndScope2.graph(tag: Any? = null): RaptorGraph? {
	fun RaptorComponentRegistry2.find(): RaptorGraph? {
		oneOrNull(RaptorGraphsComponent.Key)
			?.componentRegistry2
			?.many(RaptorGraphComponent.Key)
			?.filter { tag == null || tags(it).contains(tag) }
			?.also { check(it.size <= 1) { if (tag != null) "Found multiple graphs with tag: $tag" else "Found multiple graphs" } }
			?.firstOrNull()
			?.let { component ->
				component.endConfiguration()

				return checkNotNull(component.graph)
			}

		return parent?.find()
	}

	return componentRegistry2.find()
}
