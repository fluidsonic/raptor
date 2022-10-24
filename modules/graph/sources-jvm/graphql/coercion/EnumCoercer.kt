package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.transactions.*


internal object EnumCoercer : GNodeInputCoercer<Any>, GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	private fun GInputCoercerContext.coerceInput(input: Any?): Any {
		val context = checkNotNull(execution.raptorContext)
		val raptorType = (type as GEnumType).raptorType as EnumGraphType

		val inputScope = object : RaptorGraphInputScope, RaptorTransactionScope by context { // TODO improve

			override fun invalid(details: String?): Nothing =
				this@coerceInput.invalid(details = details)
		}

		return (input as? String)
			?.let { raptorType.parse(inputScope, it) }
			?: invalid()
	}


	override fun GNodeInputCoercerContext.coerceNodeInput(input: Any): Any =
		coerceInput((input as? GEnumValue)?.name)


	override fun GOutputCoercerContext.coerceOutput(output: Any): Any {
		val context = checkNotNull(execution.raptorContext)
		val raptorType = (type as GEnumType).raptorType as EnumGraphType

		val outputScope = object : RaptorGraphOutputScope, RaptorTransactionScope by context {}  // TODO improve

		return raptorType.serialize(outputScope, output)
	}


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Any =
		coerceInput(input)
}
