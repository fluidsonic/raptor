package io.fluidsonic.raptor

import kotlin.reflect.*


@RaptorDsl
class RaptorGraphInputObjectDefinitionBuilder<Value : Any> internal constructor(
	name: String,
	private val stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>,
	private val argumentContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
		factoryName = "factory"
	)
) : RaptorGraphStructuredTypeDefinitionBuilder<Value, GraphInputObjectDefinition<Value>>(
	name = name,
	valueClass = valueClass
),
	RaptorGraphArgumentDefinitionBuilder.Container by argumentContainer {

	private var factory: (RaptorGraphScope.() -> Value)? = null


	override fun build(description: String?, nestedDefinitions: List<GraphNamedTypeDefinition<*>>) =
		GraphInputObjectDefinition(
			arguments = argumentContainer.arguments.ifEmpty { null }
				?: error("At least one argument must be defined: argument<…>(…) { … }"),
			description = description,
			factory = checkNotNull(factory) { "The factory must be defined: factory { … }" },
			name = name,
			nestedDefinitions = nestedDefinitions,
			stackTrace = stackTrace,
			valueClass = valueClass
		)


	@RaptorDsl
	fun factory(factory: RaptorGraphScope.() -> Value) {
		check(this.factory === null) { "Cannot define multiple factories." }

		this.factory = factory
	}
}
