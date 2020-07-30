package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
class RaptorGraphOperationDefinitionBuilder<Value> internal constructor(
	private val additionalDefinitions: List<GraphNamedTypeDefinition<*>>,
	private val name: String,
	private val type: RaptorGraphOperationType,
	private val stackTrace: List<StackTraceElement>,
	private val valueType: KType,
	private val argumentsContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
		factoryName = "resolver"
	)
) : RaptorGraphArgumentDefinitionBuilder.ContainerInternal by argumentsContainer {

	private var description: String? = null
	private var isNullable = valueType.isMarkedNullable
	private var resolver: (suspend RaptorGraphScope.() -> Value)? = null


	init {
		checkGraphCompatibility(valueType)
	}


	internal fun build(): GraphOperationDefinition<Value> {
		val resolver = checkNotNull(resolver) { "The resolver must be defined: resolver { … }" }

		return GraphOperationDefinition(
			additionalDefinitions = additionalDefinitions,
			field = GraphFieldDefinition(
				arguments = argumentsContainer.arguments,
				description = description,
				name = name,
				resolver = { resolver() },
				valueType = valueType.withNullability(isNullable)
			),
			stackTrace = stackTrace,
			type = type
		)
	}


	@RaptorDsl
	fun description(description: String) {
		check(this.description === null) { "Cannot define the description more than once." }

		this.description = description
	}


	@RaptorDsl
	fun resolver(resolver: suspend RaptorGraphScope.() -> Value) {
		check(this.resolver === null) { "Cannot define multiple resolutions." }

		this.resolver = resolver
	}


	// remove once fixed: https://youtrack.jetbrains.com/issue/KT-36371
	@RaptorDsl
	fun resolverNullable(resolver: suspend RaptorGraphScope.() -> Value) {
		check(this.resolver === null) { "Cannot define multiple resolutions." }

		isNullable = true
		this.resolver = resolver
	}
}
