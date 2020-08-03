package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


// FIXME allow some kind of invalidValueError() in all Raptor parsers & serializers
internal object ScalarCoercer : GNodeInputCoercer<GValue>, GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	private fun GInputCoercerContext.coerceInput(input: Any): Any? {
		val context = checkNotNull(execution.raptorContext)
		val raptorType = (type as GCustomScalarType).raptorType as ScalarGraphType

		val inputScope = object : RaptorGraphInputScope, RaptorGraphScope by context {

			override fun invalid(details: String?): Nothing =
				this@coerceInput.invalid(details = details)
		}

		return raptorType.parse(inputScope, input)
	}


	override fun GNodeInputCoercerContext.coerceNodeInput(input: GValue): Any? =
		coerceInput(input.unwrap() ?: invalid())


	@Suppress("UNCHECKED_CAST")
	override fun GOutputCoercerContext.coerceOutput(output: Any): Any {
		val context = checkNotNull(execution.raptorContext)
		val raptorType = (type as GCustomScalarType).raptorType as ScalarGraphDefinition

		val outputScope = object : RaptorGraphOutputScope, RaptorGraphScope by context {}  // FIXME improve

		return raptorType.serialize(outputScope, output)
	}


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Any? =
		coerceInput(input)
}
