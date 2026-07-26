package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class ScalarCoercer(
	raptorType: ScalarGraphType,
) : GNodeInputCoercer<GValue>, GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	private val parse = checkNotNull(raptorType.parse) { "Scalar type '${raptorType.name}' does not coerce values." }
	private val serialize = checkNotNull(raptorType.serialize) { "Scalar type '${raptorType.name}' does not coerce values." }


	override fun GNodeInputCoercerContext.coerceNodeInput(input: GValue): Any =
		parse(GraphInputScope, input.unwrap() ?: GraphInputScope.invalid())


	override fun GOutputCoercerContext.coerceOutput(output: Any): Any =
		serialize(GraphOutputScope, output)


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Any =
		parse(GraphInputScope, input)
}
