package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*


@RaptorDsl
public class RaptorGraphOperationDefinitionBuilder<Value> internal constructor(
	private val additionalDefinitions: Collection<RaptorGraphDefinition>,
	private val kotlinType: KotlinType,
	private val name: String,
	private val operationType: RaptorGraphOperationType,
	private val stackTrace: List<StackTraceElement>,
	private val argumentContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
		factoryName = "resolver",
		parentKotlinType = kotlinType
	),
) : RaptorGraphArgumentDefinitionBuilder.ContainerInternal by argumentContainer {

	private var description: String? = null
	private var resolve: (suspend RaptorGraphOutputScope.() -> Any?)? = null


	internal fun build(): GraphOperationDefinition {
		val resolve = checkNotNull(resolve) { "The resolver must be defined: resolver { … }" }

		return GraphOperationDefinition(
			additionalDefinitions = additionalDefinitions,
			fieldDefinition = GraphFieldDefinition.Resolvable(
				argumentDefinitions = argumentContainer.argumentDefinitions,
				argumentResolver = argumentContainer.resolver,
				description = description,
				kotlinType = kotlinType,
				name = name,
				resolve = { resolve() },
				stackTrace = stackTrace
			),
			stackTrace = stackTrace,
			operationType = operationType
		)
	}


	@RaptorDsl
	public fun description(description: String) {
		check(this.description === null) { "Cannot define the description more than once." }

		this.description = description
	}


	@RaptorDsl
	public fun resolver(resolve: suspend RaptorGraphOutputScope.() -> Value) {
		check(this.resolve === null) { "Cannot define multiple resolutions." }

		this.resolve = resolve
	}
}
