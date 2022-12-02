package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
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
	internal var outputDefinition: RaptorGraphDefinition? = null

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

		val description = description?.ifEmpty { null }
		val inputFactory = checkNotNull(inputFactory) { "The input must be defined: input { … } or inputObject { … }" }
		val outputKotlinType = outputKotlinType
		val operation = operation

		// TODO hack
		if (outputKotlinType.classifier == RaptorUnion2::class) {
			additionalDefinitions += UnionGraphDefinition(
				additionalDefinitions = emptyList(),
				description = null,
				kotlinType = outputKotlinType,
				name = defaultOutputObjectName(), // TODO custom names
				stackTrace = stackTrace,
			)
		}

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
					val inputScope = object : RaptorGraphInputScope, RaptorTransactionScope by context { // TODO improve

						override fun invalid(details: String?): Nothing =
							invalidValueError("invalid argument ($details)") // TODO improve
					}

					val input = inputFactory(inputScope)

					val output = with(operation) {
						this@resolver.execute(input)
					}
					@Suppress("UNCHECKED_CAST") // TODO Hacks
					if (output is RaptorUnion2<*, *>)
						return@resolver output.value as Output

					return@resolver output
				}
			}
			.build()
	}


	private fun defaultInputObjectName() =
		name.replaceFirstChar { it.uppercase() } + "Input"


	internal fun defaultOutputObjectName() =
		name.replaceFirstChar { it.uppercase() } + "Output"


	@RaptorDsl
	public fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}


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
			// TODO configurable name & description
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
	check(outputDefinition === null) { "Cannot define multiple outputs." }

	val definition = RaptorObjectGraphDefinitionBuilder<Output>(
		kotlinType = outputKotlinType,
		name = RaptorGraphDefinition.resolveName(name, defaultName = this::defaultOutputObjectName),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()

	additionalDefinitions += definition
	outputDefinition = definition
}


@RaptorDsl
public fun <Output : Any> RaptorGraphOperationBuilder<*, Output>.outputUnion(
	name: String = RaptorGraphDefinition.defaultName,
	configure: RaptorUnionGraphDefinitionBuilder<Output>.() -> Unit,
) {
	check(outputDefinition === null) { "Cannot define multiple outputs." }

	val definition = RaptorUnionGraphDefinitionBuilder<Output>(
		kotlinType = outputKotlinType,
		name = RaptorGraphDefinition.resolveName(name, defaultName = this::defaultOutputObjectName),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()

	additionalDefinitions += definition
	outputDefinition = definition
}
