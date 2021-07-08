package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*


public sealed class RaptorGraphOperation<Input : Any, Output> {

	internal abstract val defaultNameSuffixToRemove: String
	internal abstract val type: RaptorGraphOperationType

	public abstract val definition: RaptorGraphDefinition // FIXME fun graphDefinition() like the others?

	public abstract suspend fun RaptorGraphScope.execute(input: Input): Output

	internal fun defaultName() =
		this::class.simpleName
			?.removeSuffix(defaultNameSuffixToRemove)
			?.replaceFirstChar { it.lowercase() }


	public companion object
}


public suspend fun <Output> RaptorGraphOperation<Unit, Output>.execute(context: RaptorGraphContext): Output =
	execute(context = context, input = Unit)


public suspend fun <Input : Any, Output> RaptorGraphOperation<Input, Output>.execute(context: RaptorGraphContext, input: Input): Output =
	context.asScope().execute(input)


// FIXME check that subclass name doesn't end in Query
public abstract class RaptorGraphMutation<Input : Any, Output> : RaptorGraphOperation<Input, Output>() {

	final override val defaultNameSuffixToRemove
		get() = "Mutation"


	final override val type
		get() = RaptorGraphOperationType.mutation
}


// FIXME check that subclass name doesn't end in Mutation
public abstract class RaptorGraphQuery<Input : Any, Output> : RaptorGraphOperation<Input, Output>() {

	final override val defaultNameSuffixToRemove
		get() = "Query"


	final override val type
		get() = RaptorGraphOperationType.query
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Input : Any, reified Output> RaptorGraphOperation<Input, Output>.define(
	name: String = RaptorGraphDefinition.defaultName,
	noinline configure: RaptorGraphOperationBuilder<Input, Output>.() -> Unit,
): RaptorGraphDefinition =
	define(name = name, inputType = typeOf<Input>(), outputType = typeOf<Output>(), configure = configure)


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Input : Any, reified Output> RaptorGraphOperation<Input, Output>.define(
	name: String = RaptorGraphDefinition.defaultName,
	inputArgumentName: String,
	noinline configure: RaptorGraphOperationBuilder<Input, Output>.() -> Unit = {},
): RaptorGraphDefinition =
	define(name = name, inputType = typeOf<Input>(), outputType = typeOf<Output>()) {
		input {
			// https://youtrack.jetbrains.com/issue/KT-39434 FIXME is fixed?
			val inputArgument = argument<Input> {
				name(inputArgumentName)
			}
			val inputProvider = inputArgument.provideDelegate(null, String::length)

			factory { inputProvider.getValue(null, String::length) }
		}

		configure()
	}


// FIXME validate KTypes
@RaptorDsl
public fun <Input : Any, Output> RaptorGraphOperation<Input, Output>.define(
	name: String = RaptorGraphDefinition.defaultName,
	inputType: KType,
	outputType: KType,
	configure: RaptorGraphOperationBuilder<Input, Output>.() -> Unit,
): RaptorGraphDefinition =
	RaptorGraphOperationBuilder(
		name = name,
		inputKotlinType = KotlinType.of(
			type = inputType,
			containingType = null,
			allowMaybe = true,
			allowNull = false,
			allowedVariance = KVariance.IN, // TODO prb. wrong
			requireSpecialization = true
		),
		operation = this,
		outputKotlinType = KotlinType.of(
			type = outputType,
			containingType = null,
			allowMaybe = false,
			allowNull = true,
			allowedVariance = KVariance.OUT, // TODO prb. wrong
			requireSpecialization = true
		),
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


@RaptorDsl
public inline fun <reified Output> RaptorGraphOperation<Unit, Output>.define(
	name: String = RaptorGraphDefinition.defaultName,
): RaptorGraphDefinition =
	define(name = name) {}
