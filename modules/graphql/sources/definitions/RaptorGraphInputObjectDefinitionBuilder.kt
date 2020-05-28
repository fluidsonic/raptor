package io.fluidsonic.raptor

import kotlin.reflect.*


@RaptorDsl
class RaptorGraphInputObjectDefinitionBuilder<Value : Any> internal constructor(
	private val stackTrace: List<StackTraceElement>,
	private val valueClass: KClass<Value>,
	private val defaultName: (() -> String?)? = null,
	private val argumentContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl()
) : RaptorGraphArgumentDefinitionBuilder.Container by argumentContainer {

	private var description: String? = null
	private var factory: (RaptorGraphScope.() -> Value)? = null
	private var name: String? = null


	init {
		checkGraphCompatibility(valueClass)
	}


	internal fun build() =
		GraphInputObjectDefinition(
			arguments = argumentContainer.arguments.ifEmpty { null }
				?: error("At least one argument must be defined: argument<…>(…) { … }"),
			description = description,
			factory = checkNotNull(factory) { "The factory must be defined: factory { … }" },
			name = name ?: defaultName?.invoke() ?: valueClass.defaultGraphName(),
			stackTrace = stackTrace,
			valueClass = valueClass
		)


	@RaptorDsl
	fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}


	@RaptorDsl
	fun factory(factory: RaptorGraphScope.() -> Value) {
		check(this.factory === null) { "Cannot define multiple factories." }

		this.factory = factory
	}


	@RaptorDsl
	fun name(name: String) {
		check(this.name === null) { "Cannot define multiple names." }

		this.name = name
	}
}
