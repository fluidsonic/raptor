package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


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


suspend fun <Output> RaptorGraphOperation<Unit, Output>.execute(context: RaptorGraphContext): Output =
	execute(context = context, input = Unit)


suspend fun <Input : Any, Output> RaptorGraphOperation<Input, Output>.execute(context: RaptorGraphContext, input: Input): Output =
	context.asScope().execute(input)


// FIXME check that subclass name doesn't end in Query
abstract class RaptorGraphMutation<Input : Any, Output> : RaptorGraphOperation<Input, Output>() {

	final override val defaultNameSuffixToRemove
		get() = "Mutation"


	final override val type
		get() = RaptorGraphOperationType.mutation
}


// FIXME check that subclass name doesn't end in Mutation
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


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
inline fun <reified Input : Any, reified Output> RaptorGraphOperation<Input, Output>.define(
	inputArgumentName: String,
	configure: RaptorGraphOperationBuilder<Input, Output>.() -> Unit = {}
): GraphOperationDefinition<Output> =
	define(inputClass = Input::class, outputType = typeOf<Output>()) {
		input {
			// https://youtrack.jetbrains.com/issue/KT-39434
			val inputArgument = argument<Input> {
				name(inputArgumentName)
			}
			val inputDelegate = inputArgument.provideDelegate(thisRef = null, property = null)

			factory { inputDelegate.getValue(thisRef = null, property = null) }
		}

		configure()
	}


// FIXME validate KTypes
@RaptorDsl
inline fun <Input : Any, Output> RaptorGraphOperation<Input, Output>.define(
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
