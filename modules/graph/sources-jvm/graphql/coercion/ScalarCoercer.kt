package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*


internal class ScalarCoercer(
	raptorType: ScalarGraphType,
) : GInputLiteralCoercer<GValue>, GOutputValueCoercer<Any>, GInputValueCoercer<Any> {

	private val parse = raptorType.parse
	private val serialize = raptorType.serialize


	override fun coerceInputLiteral(value: GValue): Any =
		parse(GraphInputScope, value.unwrap() ?: GraphInputScope.invalid())


	override fun coerceOutputValue(value: Any): Any =
		serialize(GraphOutputScope, value)


	override fun coerceInputValue(value: Any): Any =
		parse(GraphInputScope, value)
}
