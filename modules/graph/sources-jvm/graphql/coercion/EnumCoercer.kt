package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class EnumCoercer(
	private val raptorType: EnumGraphType,
) : GInputLiteralCoercer<Any>, GOutputValueCoercer<Any>, GInputValueCoercer<Any> {

	private fun coerceInput(input: Any?): Any =
		(input as? String)
			?.let { raptorType.parse(GraphInputScope, it) }
			?: GraphInputScope.invalid()


	override fun coerceInputLiteral(value: Any): Any =
		coerceInput((value as? GEnumValue)?.name)


	override fun coerceOutputValue(value: Any): Any =
		raptorType.serialize(GraphOutputScope, value)


	override fun coerceInputValue(value: Any): Any =
		coerceInput(value)
}
