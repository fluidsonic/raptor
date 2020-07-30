package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
class RaptorGraphOperationBuilder<Input : Any, Output> @PublishedApi internal constructor(
	name: String,
	private val inputClass: KClass<Input>,
	internal val operation: RaptorGraphOperation<Input, Output>,
	internal val outputType: KType,
	private val stackTrace: List<StackTraceElement>
) {

	internal val additionalDefinitions: MutableList<GraphNamedTypeDefinition<*>> = mutableListOf()
	internal var outputObjectDefinition: GraphObjectDefinition<*>? = null

	private val argumentsContainer = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
		factoryName = "factory"
	)
	private var description: String? = null
	private var inputFactory: (RaptorGraphScope.() -> Input)? = null

	private val name = when (name) {
		RaptorGraphDefinition.defaultName -> operation.defaultName()
			?: error("Cannot derive name from operation ${operation::class}. It must be defined explicitly: define(name = …)")
		else -> name
	}


	@PublishedApi
	internal fun build(): GraphOperationDefinition<Output> {
		@Suppress("UNCHECKED_CAST")
		if (inputFactory == null && inputClass == Unit::class)
			inputFactory = { Unit as Input }

		val arguments = argumentsContainer.arguments
		val description = description
		val inputFactory = checkNotNull(inputFactory) { "The input must be defined: input { … } or inputObject { … }" }
		val operation = operation

		return RaptorGraphOperationDefinitionBuilder<Output>(
			additionalDefinitions = additionalDefinitions,
			name = name,
			type = operation.type,
			stackTrace = stackTrace,
			valueType = outputType
		)
			.apply {
				description?.let(this::description)

				for (argument in arguments)
					add(argument)

				resolver {
					val input = inputFactory()

					with(operation) {
						this@resolver.execute(input)
					}
				}
			}
			.build()
	}


	private fun defaultInputObjectName() =
		name.capitalize() + "Input"


	internal fun defaultOutputObjectName() =
		name.capitalize() + "Output"


	@RaptorDsl
	fun input(configure: InputBuilder.() -> Unit) {
		check(inputFactory === null) { "Cannot define multiple inputs." }

		InputBuilder().apply(configure)

		check(inputFactory !== null) { "The factory must be defined: factory { … }" }
	}


	@RaptorDsl
	fun inputObject(
		name: String = RaptorGraphDefinition.defaultName,
		configure: RaptorGraphInputObjectDefinitionBuilder<Input>.() -> Unit
	) {
		check(inputFactory === null) { "Cannot define multiple inputs." }

		val definition = RaptorGraphInputObjectDefinitionBuilder(
			name = RaptorGraphDefinition.resolveName(name, defaultName = this::defaultInputObjectName),
			stackTrace = stackTrace(skipCount = 1),
			valueClass = inputClass
		)
			.apply(configure)
			.build()

		additionalDefinitions += definition

		val input by argumentsContainer.argument<Input>(valueType = inputClass.starProjectedType) {
			// FIXME configurable name & description
			name("input")
		}

		inputFactory = { input }
	}


	@RaptorDsl
	inner class InputBuilder internal constructor() : RaptorGraphArgumentDefinitionBuilder.Container by argumentsContainer {

		@RaptorDsl
		fun factory(factory: RaptorGraphScope.() -> Input) {
			check(this@RaptorGraphOperationBuilder.inputFactory === null) { "Cannot define multiple factories." }

			this@RaptorGraphOperationBuilder.inputFactory = factory
		}
	}
}


@RaptorDsl
fun <Output : Any> RaptorGraphOperationBuilder<*, Output>.outputObject(
	name: String = RaptorGraphDefinition.defaultName,
	configure: RaptorGraphObjectDefinitionBuilder<Output>.() -> Unit
) {
	check(outputObjectDefinition === null) { "Cannot define multiple outputs." }

	@Suppress("UNCHECKED_CAST")
	val definition = RaptorGraphObjectDefinitionBuilder(
		name = RaptorGraphDefinition.resolveName(name, defaultName = this::defaultOutputObjectName),
		stackTrace = stackTrace(skipCount = 1),
		valueClass = outputType.classifier as KClass<Output>
	)
		.apply(configure)
		.build()

	additionalDefinitions += definition
	outputObjectDefinition = definition
}
