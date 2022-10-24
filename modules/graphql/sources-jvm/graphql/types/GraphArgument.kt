package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*


internal class GraphArgument(
	// TODO actually a definition
	val defaultValue: GValue?,
	val description: String?,
	val directives: Collection<GraphDirective>,
	val kotlinType: KotlinType,
	val name: String,
)
