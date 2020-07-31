package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


internal object EnumCoercer : GNodeInputCoercer<Any>, GOutputCoercer<Any>, GVariableInputCoercer<Any> {

	private fun GInputCoercerContext.coerceInput(input: Any?): Enum<*> {
		val definition = (type as GEnumType).raptorTypeDefinition as GraphEnumDefinition<*>

		return (input as? String)
			?.let { name -> definition.values.firstOrNull { it.name == name } }
			?: invalid(details = "valid values: ${definition.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }}")
	}


	override fun GNodeInputCoercerContext.coerceNodeInput(input: Any): Enum<*> =
		coerceInput((input as? GEnumValue)?.name)


	override fun GOutputCoercerContext.coerceOutput(output: Any): Any =
		(output as? Enum<*>)?.name ?: invalid()


	override fun GVariableInputCoercerContext.coerceVariableInput(input: Any): Enum<*> =
		coerceInput(input)
}
