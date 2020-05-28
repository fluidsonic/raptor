package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.full.*


@RaptorDsl
class RaptorGraphOperationBuilder<Input : Any, Output> internal constructor(
	private val inputClass: KClass<Input>,
	internal val operation: RaptorGraphOperation<Input, Output>,
	internal val outputType: KType,
	private val stackTrace: List<StackTraceElement>
) {

	internal var outputObjectDefinition: GraphObjectDefinition<*>? = null

	private val argumentsContainer = RaptorGraphArgumentDefinitionBuilder.ContainerImpl()
	private var description: String? = null
	private var inputObjectDefinition: GraphInputObjectDefinition<*>? = null
	private var inputFactory: (RaptorGraphScope.() -> Input)? = null
	private var name: String? = null


	internal fun build(): GraphOperationDefinition<Output> {
		@Suppress("UNCHECKED_CAST")
		if (inputFactory == null && inputClass == Unit::class)
			inputFactory = { Unit } as RaptorGraphScope.() -> Input

		val arguments = argumentsContainer.arguments
		val description = description
		val inputFactory = checkNotNull(inputFactory) { "The input must be defined: input { … } or inputObject { … }" }
		val name = name
			?: operation.defaultName()
			?: error("Cannot derive name from operation ${operation::class}. It must be defined explicitly: define { name(\"…\") }")
		val operation = operation

		return RaptorGraphOperationDefinitionBuilder<Output>(
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


	@RaptorDsl
	fun input(configure: InputBuilder.() -> Unit) {
		check(inputFactory === null) { "Cannot define multiple inputs." }

		InputBuilder().apply(configure)

		check(inputFactory !== null) { "The factory must be defined: factory { … }" }
	}


	@RaptorDsl
	fun inputObject(configure: RaptorGraphInputObjectDefinitionBuilder<Input>.() -> Unit) {
		check(inputFactory === null) { "Cannot define multiple inputs." }

		// FIXME how to register type definition?
		inputObjectDefinition = RaptorGraphInputObjectDefinitionBuilder<Input>(
			stackTrace = stackTrace(skipCount = 1),
			valueClass = inputClass,
			defaultName = operation::defaultInputObjectName
		)
			.apply(configure)
			.build()

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
fun <Output : Any> RaptorGraphOperationBuilder<*, Output>.outputObject(configure: RaptorGraphObjectDefinitionBuilder<Output>.() -> Unit) {
	check(outputObjectDefinition === null) { "Cannot define multiple outputs." }

	// FIXME how to register type definition?
	@Suppress("UNCHECKED_CAST")
	outputObjectDefinition = RaptorGraphObjectDefinitionBuilder<Output>(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = outputType.classifier as KClass<Output>,
		defaultName = operation::defaultOutputObjectName
	)
		.apply(configure)
		.build()
}
