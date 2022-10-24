package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.transactions.*


// TODO allow some kind of invalidValueError() in all Raptor parsers & serializers
internal object ScalarCoercer : GNodeInputCoercer<GValue>, GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	private fun GInputCoercerContext.coerceInput(input: Any): Any {
		val context = checkNotNull(execution.raptorContext)
		val raptorType = (type as GCustomScalarType).raptorType as ScalarGraphType

		val inputScope = object : RaptorGraphInputScope, RaptorTransactionScope by context {

			override fun invalid(details: String?): Nothing =
				this@coerceInput.invalid(details = details)
		}

		return raptorType.parse(inputScope, input)
	}


	override fun GNodeInputCoercerContext.coerceNodeInput(input: GValue): Any =
		coerceInput(input.unwrap() ?: invalid())


	override fun GOutputCoercerContext.coerceOutput(output: Any): Any {
		val context = checkNotNull(execution.raptorContext)
		val raptorType = (type as GCustomScalarType).raptorType as ScalarGraphType

		val outputScope = object : RaptorGraphOutputScope, RaptorTransactionScope by context {}  // TODO improve

		return raptorType.serialize(outputScope, output)
	}


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Any =
		coerceInput(input)
}
