package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class InputObjectCoercer(
	private val argumentDefinitions: Collection<GArgumentDefinition>,
	private val raptorType: InputObjectGraphType,
) : GNodeInputCoercer<Map<String, Any?>>, GVariableInputCoercer<Map<String, Any?>> {

	private fun coerceInput(input: Map<String, Any?>): Any =
		raptorType.argumentResolver.withArguments(
			argumentValues = input,
			argumentDefinitions = argumentDefinitions,
		) { raptorType.create(GraphInputScope) }


	override fun GNodeInputCoercerContext.coerceNodeInput(input: Map<String, Any?>): Any =
		coerceInput(input)


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Map<String, Any?>): Any =
		coerceInput(input)
}
