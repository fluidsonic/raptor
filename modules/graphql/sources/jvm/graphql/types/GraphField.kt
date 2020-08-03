package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.raptor.*


internal sealed class GraphField(
	val arguments: Collection<GraphArgument>,
	val description: String?,
	val kotlinType: KotlinType,
	val name: String,
) {

	class Resolvable(
		val argumentResolver: ArgumentResolver,
		arguments: Collection<GraphArgument>,
		description: String?,
		kotlinType: KotlinType,
		name: String,
		val resolve: suspend RaptorGraphOutputScope.(parent: Any) -> Any?,
	) : GraphField(
		arguments = arguments,
		description = description,
		kotlinType = kotlinType,
		name = name
	)


	class Unresolvable(
		arguments: Collection<GraphArgument>,
		description: String?,
		kotlinType: KotlinType,
		name: String,
	) : GraphField(
		arguments = arguments,
		description = description,
		kotlinType = kotlinType,
		name = name
	)
}
