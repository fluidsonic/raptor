package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class InputObjectCoercer(
	private val argumentDefinitions: Collection<GArgumentDefinition>,
	private val raptorType: InputObjectGraphType,
) : GInputLiteralCoercer<Map<String, Any?>>, GInputValueCoercer<Map<String, Any?>> {

	private fun coerceInput(input: Map<String, Any?>): Any =
		raptorType.argumentResolver.withArguments(
			argumentValues = input,
			argumentDefinitions = argumentDefinitions,
		) { raptorType.create(GraphInputScope) }


	override fun coerceInputLiteral(value: Map<String, Any?>): Any =
		coerceInput(value)


	override fun coerceInputValue(value: Map<String, Any?>): Any =
		coerceInput(value)
}
