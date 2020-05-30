package io.fluidsonic.raptor

import kotlin.reflect.*


@RaptorDsl
class RaptorGraphInputObjectDefinitionBuilder<Value : Any> internal constructor(
	private val stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>,
	defaultName: (() -> String?)? = null,
	private val argumentContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl()
) : RaptorGraphStructuredTypeDefinitionBuilder<Value, GraphInputObjectDefinition<Value>>(
	defaultName = defaultName,
	valueClass = valueClass
),
	RaptorGraphArgumentDefinitionBuilder.Container by argumentContainer {

	private var factory: (RaptorGraphScope.() -> Value)? = null


	override fun build(description: String?, name: String, nestedDefinitions: List<GraphNamedTypeDefinition<*>>) =
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
