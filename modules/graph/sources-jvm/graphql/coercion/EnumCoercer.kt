package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class EnumCoercer(
	private val raptorType: EnumGraphType,
) : GNodeInputCoercer<Any>, GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	private fun coerceInput(input: Any?): Any =
		(input as? String)
			?.let { raptorType.parse(GraphInputScope, it) }
			?: GraphInputScope.invalid()


	override fun GNodeInputCoercerContext.coerceNodeInput(input: Any): Any =
		coerceInput((input as? GEnumValue)?.name)


	override fun GOutputCoercerContext.coerceOutput(output: Any): Any =
		raptorType.serialize(GraphOutputScope, output)


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Any =
		coerceInput(input)
}
