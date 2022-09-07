package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


internal object InputObjectCoercer : GNodeInputCoercer<Map<String, Any?>>, GVariableInputCoercer<Map<String, Any?>> {

	private fun GInputCoercerContext.coerceInput(input: Map<String, Any?>): Any? {
		val context = checkNotNull(execution.raptorContext)
		val type = type as GInputObjectType
		val definition = type.raptorType as InputObjectGraphType

		val inputScope = object : RaptorGraphInputScope, RaptorGraphScope by context { // FIXME improve

			override fun invalid(details: String?): Nothing =
				this@coerceInput.invalid(details = details)
		}

		return definition.argumentResolver.withArguments(
			argumentValues = input,
			argumentDefinitions = type.argumentDefinitions,
			context = execution
		) { definition.create(inputScope) }
	}


	override fun GNodeInputCoercerContext.coerceNodeInput(input: Map<String, Any?>): Any? =
		coerceInput(input)


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Map<String, Any?>): Any? =
		coerceInput(input)
}
