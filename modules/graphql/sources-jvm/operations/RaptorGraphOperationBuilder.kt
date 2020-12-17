package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*


@RaptorDsl
public class RaptorGraphOperationBuilder<Input : Any, Output> @PublishedApi internal constructor(
	name: String,
	private val inputKotlinType: KotlinType,
	internal val operation: RaptorGraphOperation<Input, Output>,
	internal val outputKotlinType: KotlinType,
	private val stackTrace: List<StackTraceElement>,
) {

	internal val additionalDefinitions: MutableList<RaptorGraphDefinition> = mutableListOf()
	internal var outputObjectDefinition: RaptorGraphDefinition? = null

	private val argumentContainer = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
		factoryName = "factory",
		parentKotlinType = inputKotlinType
	)
	private var description: String? = null
	private var inputFactory: (RaptorGraphInputScope.() -> Input)? = null

	private val name = when (name) {
		RaptorGraphDefinition.defaultName -> operation.defaultName()
			?: error("Cannot derive name from operation ${operation::class}. It must be defined explicitly: define(name = …)")
		else -> name
	}


	@PublishedApi
	internal fun build(): RaptorGraphDefinition {
		@Suppress("UNCHECKED_CAST")
		if (inputFactory == null && inputKotlinType.classifier == Unit::class)
			inputFactory = { Unit as Input }

		val description = description
		val inputFactory = checkNotNull(inputFactory) { "The input must be defined: input { … } or inputObject { … }" }
		val operation = operation

		return RaptorGraphOperationDefinitionBuilder<Output>(
			additionalDefinitions = additionalDefinitions,
			kotlinType = outputKotlinType,
			name = name,
			operationType = operation.type,
			stackTrace = stackTrace,
			argumentContainer = argumentContainer
		)
			.apply {
				description?.let(this::description)

				resolver {
					val inputScope = object : RaptorGraphInputScope, RaptorGraphScope by context { // FIXME improve

						override fun invalid(details: String?): Nothing =
							error("invalid argument") // FIXME
					}

					val input = inputFactory(inputScope)

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
	public fun input(configure: InputBuilder.() -> Unit) {
		check(inputFactory === null) { "Cannot define multiple inputs." }

		InputBuilder().apply(configure)

		check(inputFactory !== null) { "The factory must be defined: factory { … }" }
	}


	@RaptorDsl
	public fun inputObject(
		name: String = RaptorGraphDefinition.defaultName,
		configure: RaptorInputObjectGraphDefinitionBuilder<Input>.() -> Unit,
	) {
		check(inputFactory === null) { "Cannot define multiple inputs." }

		val definition = RaptorInputObjectGraphDefinitionBuilder<Input>(
			kotlinType = inputKotlinType,
			name = RaptorGraphDefinition.resolveName(name, defaultName = this::defaultInputObjectName),
			stackTrace = stackTrace(skipCount = 1)
		)
			.apply(configure)
			.build()

		additionalDefinitions += definition

		val input by argumentContainer.argument<Input>(type = inputKotlinType) {
			// FIXME configurable name & description
			name("input")
		}

		inputFactory = { input }
	}


	@RaptorDsl
	public inner class InputBuilder internal constructor() : RaptorGraphArgumentDefinitionBuilder.Container by argumentContainer {

		@RaptorDsl
		public fun factory(factory: RaptorGraphInputScope.() -> Input) {
			check(this@RaptorGraphOperationBuilder.inputFactory === null) { "Cannot define multiple factories." }

			this@RaptorGraphOperationBuilder.inputFactory = factory
		}
	}
}


@RaptorDsl
public fun <Output : Any> RaptorGraphOperationBuilder<*, Output>.outputObject(
	name: String = RaptorGraphDefinition.defaultName,
	configure: RaptorObjectGraphDefinitionBuilder<Output>.() -> Unit,
) {
	check(outputObjectDefinition === null) { "Cannot define multiple outputs." }

	@Suppress("UNCHECKED_CAST")
	val definition = RaptorObjectGraphDefinitionBuilder<Output>(
		kotlinType = outputKotlinType,
		name = RaptorGraphDefinition.resolveName(name, defaultName = this::defaultOutputObjectName),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()

	additionalDefinitions += definition
	outputObjectDefinition = definition
}
