package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public val RaptorContext.graphs: Collection<RaptorGraph>
	get() = properties[RaptorGraphsPropertyKey]
		?: error("You must install ${RaptorGraphFeature::class.simpleName} for enabling GraphQL functionality.")


public fun RaptorContext.graph(tag: Any): RaptorGraph =
	graphs.singleOrNull { it.tags.contains(tag) } ?: error(when {
		graphs.any { it.tags.contains(tag) } -> "Multiple graphs have the same tag: $tag"
		else -> "There is no graph with this tag: $tag"
	})
