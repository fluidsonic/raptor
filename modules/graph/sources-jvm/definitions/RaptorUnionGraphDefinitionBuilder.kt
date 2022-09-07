package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


@RaptorDsl
public class RaptorUnionGraphDefinitionBuilder<Type : Any> internal constructor(
	kotlinType: KotlinType,
	name: String,
	private val stackTrace: List<StackTraceElement>,
) : RaptorStructuredGraphTypeDefinitionBuilder<Type>(
	kotlinType = kotlinType,
	name = name
) {

	override fun build(description: String?, additionalDefinitions: Collection<RaptorGraphDefinition>) =
		UnionGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			kotlinType = kotlinType,
			name = name,
			stackTrace = stackTrace
		)
}
