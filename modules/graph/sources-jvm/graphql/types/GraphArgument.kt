package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class GraphArgument(
	// FIXME actually a definition
	val defaultValue: GValue?,
	val description: String?,
	val directives: Collection<GraphDirective>,
	val kotlinType: KotlinType,
	val name: String,
)
