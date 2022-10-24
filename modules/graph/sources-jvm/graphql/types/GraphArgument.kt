package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class GraphArgument(
	// TODO This is actually an argument definition, not an argument.
	val defaultValue: GValue?,
	val description: String?,
	val directives: Collection<GraphDirective>,
	val kotlinType: KotlinType,
	val name: String,
)
