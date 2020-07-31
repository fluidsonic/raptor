package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


internal object InputObjectCoercer : GNodeInputCoercer<Map<String, Any?>>, GVariableInputCoercer<Map<String, Any?>> {

	private fun GInputCoercerContext.coerceInput(input: Map<String, Any?>): Any? {
		val context = checkNotNull(execution.raptorContext)
		val type = type as GInputObjectType
		val definition = type.raptorTypeDefinition as GraphInputObjectDefinition<*>

		return definition.argumentResolver.withArguments(
			argumentValues = input,
			argumentDefinitions = type.argumentDefinitions,
			context = execution
		) { definition.factory(context) }
	}


	override fun GNodeInputCoercerContext.coerceNodeInput(input: Map<String, Any?>): Any? =
		coerceInput(input)


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Map<String, Any?>): Any? =
		coerceInput(input)
}
