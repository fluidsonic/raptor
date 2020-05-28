package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


@RaptorDsl
sealed class RaptorGraphOperation<Input : Any, Output> {

	internal abstract val defaultNameSuffixToRemove: String
	internal abstract val type: RaptorGraphOperationType

	abstract val definition: GraphOperationDefinition<Output> // FIXME fun graphDefinition() like the others?

	abstract suspend fun RaptorGraphScope.execute(input: Input): Output


	internal fun defaultInputObjectName() =
		this::class.simpleName
			?.removeSuffix(defaultNameSuffixToRemove)
			?.capitalize()
			?.plus("Input")


	internal fun defaultName() =
		this::class.simpleName
			?.removeSuffix(defaultNameSuffixToRemove)
			?.decapitalize()


	internal fun defaultOutputObjectName() =
		this::class.simpleName
			?.removeSuffix(defaultNameSuffixToRemove)
			?.capitalize()
			?.plus("Output")


	companion object
}


@RaptorDsl
abstract class RaptorGraphMutation<Input : Any, Output> : RaptorGraphOperation<Input, Output>() {

	final override val defaultNameSuffixToRemove
		get() = "Mutation"


	final override val type
		get() = RaptorGraphOperationType.mutation
}


@RaptorDsl
abstract class RaptorGraphQuery<Input : Any, Output> : RaptorGraphOperation<Input, Output>() {

	final override val defaultNameSuffixToRemove
		get() = "Query"


	final override val type
		get() = RaptorGraphOperationType.query
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
inline fun <reified Input : Any, reified Output> RaptorGraphOperation<Input, Output>.define(
	noinline configure: RaptorGraphOperationBuilder<Input, Output>.() -> Unit
): GraphOperationDefinition<Output> =
	define(inputClass = Input::class, outputType = typeOf<Output>(), configure = configure)


// FIXME validate KTypes
@RaptorDsl
fun <Input : Any, Output> RaptorGraphOperation<Input, Output>.define(
	inputClass: KClass<Input>,
	outputType: KType,
	configure: RaptorGraphOperationBuilder<Input, Output>.() -> Unit
): GraphOperationDefinition<Output> =
	RaptorGraphOperationBuilder(
		inputClass = inputClass,
		operation = this,
		outputType = outputType,
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
inline fun <reified Output> RaptorGraphOperation<Unit, Output>.define(): GraphOperationDefinition<Output> =
	define {}
