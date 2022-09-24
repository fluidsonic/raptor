package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


public fun RaptorContext.graph(tag: Any): RaptorGraph =
	graphs.singleOrNull { it.tags.contains(tag) } ?: error(when {
		graphs.any { it.tags.contains(tag) } -> "Multiple graphs have the same tag: $tag"
		else -> "There is no graph with this tag: $tag"
	})
